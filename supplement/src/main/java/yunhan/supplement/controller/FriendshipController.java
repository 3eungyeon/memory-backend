
package yunhan.supplement.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import yunhan.supplement.DTO.FriendRequestDTO;
import yunhan.supplement.Entity.UserEntity;
import yunhan.supplement.Service.FriendshipService;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/friendship")
@RequiredArgsConstructor
public class FriendshipController {
    private final FriendshipService friendshipService;

    @PostMapping("/request")
    public ResponseEntity<Map<String, Object>> sendFriendRequest(@RequestBody FriendRequestDTO requestDTO) {
        friendshipService.sendFriendRequest(requestDTO.getSenderId(), requestDTO.getReceiverId());
        return ResponseEntity.ok(Map.of("status", "success", "message", "친구 요청을 보냈습니다."));
    }

    @PostMapping("/accept")
    public ResponseEntity<Map<String, Object>> acceptFriendRequest(@RequestBody FriendRequestDTO requestDTO) {
        friendshipService.acceptFriendRequest(requestDTO.getSenderId(), requestDTO.getReceiverId());
        return ResponseEntity.ok(Map.of("status", "success", "message", "친구 요청을 수락했습니다."));
    }

    @DeleteMapping("/reject")
    public ResponseEntity<Map<String, Object>> rejectFriendRequest(@RequestBody FriendRequestDTO requestDTO) {
        friendshipService.rejectFriendRequest(requestDTO.getSenderId(), requestDTO.getReceiverId());
        return ResponseEntity.ok(Map.of("status", "success", "message", "친구 요청을 거절했습니다."));
    }

    @GetMapping("/friends")
    public CompletableFuture<ResponseEntity<Map<String, Object>>> getFriends(@RequestParam int userId) {
        return friendshipService.getFriendsAsync(userId)
                .thenApply(friends -> {
                    List<Map<String, Object>> friendList = friends.stream().map(friend -> {
                        Map<String, Object> map = new HashMap<>();
                        map.put("id", friend.getId());
                        map.put("username", friend.getUsername());
                        map.put("name", friend.getName());
                        return map;
                    }).collect(Collectors.toList());
                    Collections.reverse(friendList);
                    return ResponseEntity.ok(Map.of("status", "success", "friends", friendList));
                });
    }

    @GetMapping("/following")
    public CompletableFuture<ResponseEntity<Map<String, Object>>> getFollowingList(@RequestParam int userId) {
        return friendshipService.getFollowingListAsync(userId)
                .thenApply(list -> {
                    List<Map<String, Object>> followingList = list.stream().map(user -> {
                        Map<String, Object> map = new HashMap<>();
                        map.put("id", user.getId());
                        map.put("username", user.getUsername());
                        map.put("name", user.getName());
                        return map;
                    }).collect(Collectors.toList());
                    return ResponseEntity.ok(Map.of("status", "success", "following", followingList));
                });
    }

    @DeleteMapping("/remove")
    public ResponseEntity<Map<String, Object>> removeFriend(@RequestBody FriendRequestDTO requestDTO) {
        try {
            friendshipService.removeFriend(requestDTO.getSenderId(), requestDTO.getReceiverId());
            return ResponseEntity.ok(Map.of("status", "success", "message", "친구가 삭제되었습니다."));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("status", "error", "message", e.getMessage()));
        }
    }
}
