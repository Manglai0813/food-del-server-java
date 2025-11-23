package com.fooddel.service.impl;

import com.fooddel.dto.request.UserRequest;
import com.fooddel.entity.User;
import com.fooddel.exception.custom.BusinessException;
import com.fooddel.mapper.UserMapper;
import com.fooddel.repository.UserRepository;
import com.fooddel.service.UserService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * ユーザー関連のビジネスロジックを実装するサービス
 * Spring SecurityのUserDetailsServiceを実装します。
 */
@Service
@Lazy // Add @Lazy annotation here
public class UserServiceImpl implements UserService, UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final com.fooddel.repository.OrderRepository orderRepository;

    // コンストラクタインジェクション
    public UserServiceImpl(UserRepository userRepository, @Lazy PasswordEncoder passwordEncoder, UserMapper userMapper,
            @Lazy com.fooddel.repository.OrderRepository orderRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userMapper = userMapper;
        this.orderRepository = orderRepository;
    }

    /**
     * Spring Securityが認証時にユーザーを検索するために使用するメソッド
     * 
     * @param username ユーザー名（このアプリケーションではメールアドレス）
     * @return UserDetailsオブジェクト
     * @throws UsernameNotFoundException ユーザーが見つからない場合
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("指定されたメールアドレスのユーザーが見つかりません: " + username));
    }

    // 新しいユーザーを作成します。
    @Override
    @Transactional
    public User createUser(UserRequest userRequest) {
        // メールアドレスの一意性チェック
        userRepository.findByEmail(userRequest.getEmail())
                .ifPresent(user -> {
                    throw new BusinessException(HttpStatus.CONFLICT, "EMAIL_ALREADY_EXISTS",
                            "指定されたメールアドレスは既に使用されています。");
                });

        // DTOからエンティティへの変換
        User user = userMapper.toEntity(userRequest);

        // パスワードの暗号化
        user.setPassword(passwordEncoder.encode(userRequest.getPassword()));

        // TODO: デフォルトのロール設定などをここで行う

        return userRepository.save(user);
    }

    // 指定されたIDのユーザーを検索します。
    @Override
    @Transactional(readOnly = true)
    public Optional<User> findUserById(Integer id) {
        return userRepository.findById(id);
    }

    // 指定されたメールアドレスのユーザーを検索します。
    @Override
    @Transactional(readOnly = true)
    public Optional<User> findUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    // 指定されたIDのユーザー情報を更新します。
    @Override
    @Transactional
    public User updateUser(Integer id, UserRequest userRequest) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "指定されたユーザーが見つかりません。"));

        // メールアドレスが変更された場合、一意性チェック
        Optional.ofNullable(userRequest.getEmail())
                .filter(newEmail -> !newEmail.equals(existingUser.getEmail()))
                .ifPresent(newEmail -> userRepository.findByEmail(newEmail)
                        .ifPresent(user -> {
                            throw new BusinessException(HttpStatus.CONFLICT, "EMAIL_ALREADY_EXISTS",
                                    "指定されたメールアドレスは既に使用されています。");
                        }));

        // マッパーを使用してエンティティを更新（パスワードは別途処理）
        userMapper.updateEntityFromRequest(userRequest, existingUser);

        // パスワードがリクエストに含まれている場合は、エンコードして更新
        Optional.ofNullable(userRequest.getPassword())
                .ifPresent(password -> existingUser.setPassword(passwordEncoder.encode(password)));

        return userRepository.save(existingUser);
    }

    // 指定されたIDのユーザーを削除します。
    @Override
    @Transactional
    public void deleteUser(Integer id) {
        // 注文が存在するユーザーは削除できないようにする
        if (orderRepository.existsByUserId(id)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "CANNOT_DELETE_USER_WITH_ORDERS",
                    "注文履歴が存在するユーザーは削除できません。");
        }

        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "指定されたユーザーが見つかりません。"));

        userRepository.delete(user);
    }

    // 全てのユーザーをページネーション付きで取得します。
    @Override
    @Transactional(readOnly = true)
    public Page<User> findAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    // ユーザーのリフレッシュトークンを保存します。
    @Override
    @Transactional
    public void saveUserRefreshToken(String email, String refreshToken) {
        userRepository.findByEmail(email)
                .ifPresentOrElse(user -> {
                    user.setRefreshToken(refreshToken);
                    userRepository.save(user);
                }, () -> {
                    throw new BusinessException(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "指定されたユーザーが見つかりません。");
                });
    }

    // リフレッシュトークンでユーザーを検索します。
    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByRefreshToken(String refreshToken) {
        return userRepository.findByRefreshToken(refreshToken);
    }
}
