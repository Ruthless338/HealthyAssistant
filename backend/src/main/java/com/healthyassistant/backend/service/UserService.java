package com.healthyassistant.backend.service;

import com.healthyassistant.backend.model.User;
import com.healthyassistant.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service // This annotation indicates that this class is a service component in the Spring context.
public class UserService {
    @Autowired
    private UserRepository userRepository; //UserRepository实例，用于访问数据库

    public User authenticateUser(String username, String password) {
        User user = userRepository.findByUsername(username);
        if (user != null && user.getPassword().equals(password)) {
            return user;
        }
        return null;
    }

    public boolean register(String username, String password){
        if (userRepository.findByUsername(username) != null){
            return false;
        }
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        userRepository.save(user);
        return true;
    }

    public boolean update(User user){
        User existingUser = userRepository.findByUsername(user.getUsername());
        if (existingUser == null) {
            return false; // 用户不存在，更新失败
        }
        existingUser.setAge(user.getAge());
        existingUser.setHeight(user.getHeight());
        existingUser.setWeight(user.getWeight());
        existingUser.setGoal(user.getGoal());
        existingUser.setInterest(user.getInterest());
        existingUser.setPart(user.getPart());
        existingUser.setAvatar("https://image.baidu.com/search/detail?ct=503316480&z=0&ipn=d&word=%E6%9D%8E%E6%98%9F%E4%BA%91&hs=0&pn=0&spn=0&di=7490230549689139201&pi=0&rn=1&tn=baiduimagedetail&is=1353190716%2C287937450&ie=utf-8&oe=utf-8&cl=2&lm=-1&cs=2472999219%2C3664644135&os=1353190716%2C287937450&simid=4138114646%2C684454357&adpicid=0&lpn=0&ln=0&fm=&sme=&cg=&bdtype=0&oriquery=&objurl=https%3A%2F%2Fp3-pc-sign.douyinpic.com%2Ftos-cn-i-0813c001%2FoMQqgABA7aJdA1MNeWLR8Det8VYB3QAAGSTuEf~tplv-dy-aweme-images%3Aq75.webp&fromurl=ippr_z2C%24qAzdH3FAzdH3Fooo_z%26e3B157ytg_z%26e3Bv54AzdH3Fg5pjAzdH3F09adnlbldm0babl9c8m&gsm=&islist=&querylist=");
        existingUser.setGender(user.getGender());
        userRepository.save(existingUser);
        return true; // 更新成功
    }
}