
package yunhan.supplement.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import yunhan.supplement.Entity.Friendship;
import yunhan.supplement.Entity.UserEntity;
import yunhan.supplement.Repository.FriendshipRepository;
import yunhan.supplement.Repository.UserRepository;
import yunhan.supplement.mapper.FriendMapper;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
public class FriendshipService {
    @Autowired private FriendMapper friendMapper;
    private final FriendshipRepository friendshipRepository;
    private final UserRepository usersRepository;

    public FriendshipService(FriendshipRepository friendshipRepository, UserRepository usersRepository) {
        this.friendshipRepository = friendshipRepository;
        this.usersRepository = usersRepository;
    }

    @Cacheable(cacheNames = "friendsByUser", key = "#userId")
    public List<UserEntity> getFriendsSync(int userId) {
        List<UserEntity> friends = friendMapper.findFriendsByUserId(userId);
        return friends == null ? Collections.emptyList() : friends;
    }

    @Async("appExecutor")
    public CompletableFuture<List<UserEntity>> getFriendsAsync(int userId) {
        return CompletableFuture.supplyAsync(() -> getFriendsSync(userId));
    }

    @Async("appExecutor")
    public CompletableFuture<List<UserEntity>> getFollowingListAsync(int userId) {
        return CompletableFuture.supplyAsync(() -> {
            UserEntity sender = usersRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("잘못된 userId"));
            return friendshipRepository.findBySenderAndIsFriendFalse(sender)
                    .stream().map(Friendship::getReceiver).collect(Collectors.toList());
        });
    }

    @CacheEvict(cacheNames = "friendsByUser", key = "#senderId")
    public void sendFriendRequest(int senderId, int receiverId) {
        UserEntity sender = usersRepository.findById(senderId)
                .orElseThrow(() -> new IllegalArgumentException("잘못된 senderId"));
        UserEntity receiver = usersRepository.findById(receiverId)
                .orElseThrow(() -> new IllegalArgumentException("잘못된 receiverId"));
        if (senderId == receiverId) throw new IllegalStateException("자기 자신에게 친구 요청을 보낼 수 없습니다.");
        if (friendshipRepository.findBySenderAndReceiver(sender, receiver).isPresent())
            throw new IllegalStateException("이미 친구 요청을 보냈거나 친구입니다.");

        friendshipRepository.save(new Friendship(sender, receiver, true));
        friendshipRepository.save(new Friendship(receiver, sender, false));

        evictFriendCache(receiverId);
    }

    @CacheEvict(cacheNames = "friendsByUser", key = "#receiverId")
    public void acceptFriendRequest(int senderId, int receiverId) {
        UserEntity sender = usersRepository.findById(senderId)
                .orElseThrow(() -> new IllegalArgumentException("잘못된 senderId"));
        UserEntity receiver = usersRepository.findById(receiverId)
                .orElseThrow(() -> new IllegalArgumentException("잘못된 receiverId"));

        Friendship friendship = friendshipRepository.findBySenderAndReceiver(receiver, sender)
                .orElseThrow(() -> new IllegalStateException("친구 요청이 없습니다."));
        friendship.setFriend(true);
        friendshipRepository.save(friendship);

        evictFriendCache(senderId);
        evictFriendCache(receiverId);
    }

    @CacheEvict(cacheNames = "friendsByUser", key = "#senderId")
    public void rejectFriendRequest(int senderId, int receiverId) {
        UserEntity sender = usersRepository.findById(senderId)
                .orElseThrow(() -> new IllegalArgumentException("잘못된 senderId"));
        UserEntity receiver = usersRepository.findById(receiverId)
                .orElseThrow(() -> new IllegalArgumentException("잘못된 receiverId"));

        Friendship f1 = friendshipRepository.findBySenderAndReceiver(sender, receiver)
                .orElseThrow(() -> new IllegalStateException("친구 요청이 없습니다."));
        friendshipRepository.delete(f1);

        Friendship f2 = friendshipRepository.findBySenderAndReceiver(receiver, sender)
                .orElseThrow(() -> new IllegalStateException("친구 요청이 없습니다."));
        friendshipRepository.delete(f2);

        evictFriendCache(senderId);
        evictFriendCache(receiverId);
    }

    public void removeFriend(int userId, int friendId) {
        UserEntity user = usersRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("잘못된 userId"));
        UserEntity friend = usersRepository.findById(friendId)
                .orElseThrow(() -> new IllegalArgumentException("잘못된 friendId"));

        var existing = friendshipRepository.findBySenderAndReceiver(user, friend);
        var reverse = friendshipRepository.findByReceiverAndSender(user, friend);

        if (existing.isPresent() || reverse.isPresent()) {
            friendshipRepository.deleteFriendship(user, friend);
            evictFriendCache(userId);
            evictFriendCache(friendId);
        } else {
            throw new IllegalStateException("삭제할 친구 관계가 존재하지 않습니다.");
        }
    }

    private void evictFriendCache(int userId) {
        try {
            var ctx = org.springframework.web.context.ContextLoader.getCurrentWebApplicationContext();
            if (ctx == null) return;
            var cm = ctx.getBean(org.springframework.cache.CacheManager.class);
            var cache = cm.getCache("friendsByUser");
            if (cache != null) cache.evict(userId);
        } catch (Exception ignore) {}
    }
}


