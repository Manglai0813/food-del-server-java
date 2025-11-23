package com.fooddel.service;

import com.fooddel.dto.request.UserRequest;
import com.fooddel.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

/**
 * ユーザー関連のビジネスロジックを定義するサービスインターフェース
 */
public interface UserService {

    // 新しいユーザーを作成します。
    User createUser(UserRequest userRequest);

    // 指定されたIDのユーザーを検索します。
    Optional<User> findUserById(Integer id);

    // 指定されたメールアドレスのユーザーを検索します。
    Optional<User> findUserByEmail(String email);

    // 指定されたIDのユーザー情報を更新します。
    User updateUser(Integer id, UserRequest userRequest);

    // 指定されたIDのユーザーを削除します。
    void deleteUser(Integer id);

    // 全てのユーザーをページネーション付きで取得します。
    Page<User> findAllUsers(Pageable pageable);

    // ユーザーのリフレッシュトークンを保存します。
    void saveUserRefreshToken(String email, String refreshToken);

    // リフレッシュトークンでユーザーを検索します。
    Optional<User> findByRefreshToken(String refreshToken);
}
