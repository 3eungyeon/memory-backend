package yunhan.supplement.DTO;

import lombok.*;
import yunhan.supplement.Entity.UserEntity;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class UserDTO {
//    private Long id;
    private int id;
    private String username;
    private String password;
    private String name;

    public static UserDTO fromEntity(UserEntity userEntity) {
        return new UserDTO(
                userEntity.getId(),
                userEntity.getUsername(),
                userEntity.getPassword(),
                userEntity.getName()
        );
    }

    public static UserEntity toEntity(UserDTO userDTO) {
        return UserEntity.builder()
                .id(userDTO.getId())
                .username(userDTO.getUsername())
                .password(userDTO.getPassword())
                .name(userDTO.getName())
                .build();
    }
}
