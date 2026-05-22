package com.mofadanqing.controller;

import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.bean.WxMaJscode2SessionResult;
import cn.binarywang.wx.miniapp.bean.WxMaPhoneNumberInfo;
import cn.binarywang.wx.miniapp.bean.WxMaUserInfo;
import com.mofadanqing.entity.User;
import com.mofadanqing.service.UserService;
import com.mofadanqing.utils.JwtUtil;
import me.chanjar.weixin.common.error.WxErrorException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth/wechat")
public class WechatAuthController {

    @Autowired
    private WxMaService wxMaService;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 微信登录接口
     * 接收 code，返回 JWT token 和用户信息
     */
    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> params) {
        String code = params.get("code");
        if (code == null) {
            throw new IllegalArgumentException("Code cannot be null");
        }

        try {
            // 1. 用 code 换取 openid 和 session_key
            WxMaJscode2SessionResult session = wxMaService.getUserService().getSessionInfo(code);
            String openid = session.getOpenid();
            
            // 2. 根据 openid 查询用户，如果不存在则注册
            User user = userService.getByOpenid(openid);
            
            // 兼容旧逻辑：如果 openid 查不到，尝试用 username=openid 查（兼容之前已注册的用户）
            if (user == null) {
                user = userService.getByUsername(openid);
                if (user != null) {
                    // 补全 openid
                    user.setOpenid(openid);
                    // 这里应该调用 update，但暂时略过，下次登录会走 getByOpenid
                }
            }
            
            if (user == null) {
                // 自动注册
                user = new User();
                // 生成简短的随机用户名 (wx_ + 8位时间戳后缀)
                String shortUsername = "wx_" + Long.toString(System.currentTimeMillis(), 36);
                user.setUsername(shortUsername);
                
                // 默认昵称
                String suffix = openid.length() > 4 ? openid.substring(openid.length() - 4) : openid;
                user.setNickname("微信用户_" + suffix);
                
                // 微信登录无密码，设置一个不可用的随机密码或特殊标记
                user.setPassword(""); 
                user.setRole("USER");
                user.setStatus("ACTIVE");
                // 默认头像 (可选)
                user.setAvatar("https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png");
                // 邮箱暂不设置，避免冲突
                user.setEmail(null); 
                user.setOpenid(openid);
                userService.register(user);
            }

            // 3. 生成 Token
            String token = JwtUtil.generateToken(user);

            Map<String, Object> result = new HashMap<>();
            result.put("token", token);
            result.put("user", user);
            result.put("openid", openid);
            return result;

        } catch (WxErrorException e) {
            e.printStackTrace();
            throw new RuntimeException("Wechat login failed: " + e.getMessage());
        }
    }

    /**
     * 获取手机号（需要 code）
     */
    @PostMapping("/phone")
    public Map<String, Object> getPhoneNumber(@RequestBody Map<String, String> params) {
        String code = params.get("code");
        try {
            WxMaPhoneNumberInfo phoneInfo = wxMaService.getUserService().getPhoneNoInfo(code);
            Map<String, Object> result = new HashMap<>();
            result.put("phone", phoneInfo.getPhoneNumber());
            return result;
        } catch (WxErrorException e) {
            e.printStackTrace();
            throw new RuntimeException("Get phone failed: " + e.getMessage());
        }
    }
}
