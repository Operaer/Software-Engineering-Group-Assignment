package com.bupt.ta.storage;

import com.bupt.ta.model.User;
import com.bupt.ta.config.AppConfig;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import javax.servlet.ServletContext;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class UserStorage {
    private static final String USERS_FILE = AppConfig.USERS_FILE;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final File usersFile;

    public UserStorage(ServletContext context) {
        this.usersFile = new File(context.getRealPath(USERS_FILE));
        if (!usersFile.getParentFile().exists()) {
            usersFile.getParentFile().mkdirs();
        }
    }

    // 加载所有用户
    private Map<String, User> loadUsers() throws IOException {
        if (!usersFile.exists()) {
            return new HashMap<>();
        }
        return objectMapper.readValue(usersFile, new TypeReference<Map<String, User>>() {});
    }

    // 保存所有用户
    private void saveUsers(Map<String, User> users) throws IOException {
        objectMapper.writeValue(usersFile, users);
    }

    // 根据email查找用户
    public User findByEmail(String email) throws IOException {
        Map<String, User> users = loadUsers();
        return users.get(email);
    }

    // 验证用户登录
    public User authenticate(String email, String password) throws IOException {
        User user = findByEmail(email);
        if (user != null && user.getPassword().equals(password) && user.isActive()) {
            return user;
        }
        return null;
    }

    // 创建新用户（用于注册或管理员添加）
    public void createUser(User user) throws IOException {
        Map<String, User> users = loadUsers();
        users.put(user.getEmail(), user);
        saveUsers(users);
    }

    // 更新用户
    public void updateUser(User user) throws IOException {
        Map<String, User> users = loadUsers();
        users.put(user.getEmail(), user);
        saveUsers(users);
    }

    // 删除用户
    public void deleteUser(String email) throws IOException {
        Map<String, User> users = loadUsers();
        users.remove(email);
        saveUsers(users);
    }

    // 获取所有用户（管理员用）
    public Map<String, User> getAllUsers() throws IOException {
        return loadUsers();
    }
}