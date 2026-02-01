package com.example.demo.service.impl;

import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public User saveUser(User user) {
        return userRepository.save(user);
    }

    @Override
    public User getUserById(Long id) {
        Optional<User> optionalUser = userRepository.findById(id);
        return optionalUser.orElse(null);
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public User updateUser(Long id, User user) {
        Optional<User> optionalUser = userRepository.findById(id);
        if (optionalUser.isPresent()) {
            User existingUser = optionalUser.get();
            existingUser.setName(user.getName());
            existingUser.setEmail(user.getEmail());
            existingUser.setPhone(user.getPhone());
            return userRepository.save(existingUser);
        }
        return null;
    }

    @Override
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    @Override
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    /**
     * 复杂业务流程示例：
     * - IO 读 1：从数据库读取用户
     * - 对象修改 1：更新本地字段
     * - IO 写 1：保存到数据库
     * - IO 读 2：发起 HTTP GET 请求获取外部数据
     * - 对象修改 2：根据外部数据再次修改对象
     * - IO 写 2：再次保存到数据库
     */
    @Override
    public User complexUserFlow(Long id) {
        // IO 读 1：读取用户
        Optional<User> optional = userRepository.findById(id);
        if (optional.isEmpty()) {
            return null;
        }
        User user = optional.get();

        // 对象修改 1
        user.setName(user.getName() + " - step1");

        // IO 写 1：保存中间状态
        user = userRepository.save(user);

        // IO 读 2：调用外部 HTTP 服务（示例使用 httpbin.org）
        RestTemplate restTemplate = new RestTemplate();
        String httpResult = restTemplate.getForObject("https://httpbin.org/get", String.class);

        // 对象修改 2：根据 HTTP 结果做一点简单标记（真实场景可解析 JSON）
        user.setPhone("ext:" + (httpResult != null ? Math.min(httpResult.length(), 16) : 0));

        // IO 写 2：保存最终结果
        user = userRepository.save(user);

        return user;
    }
}