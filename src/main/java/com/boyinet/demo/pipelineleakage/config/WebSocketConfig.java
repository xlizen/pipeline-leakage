package com.boyinet.demo.pipelineleakage.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * 聊天室配置类
 *
 * @author lengchunyun
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        //允许使用socketJs方式访问，访问点为webSocket，允许跨域
        //在网页上我们就可以通过这个链接
        //ws://127.0.0.1:2222/webSocket来和服务器的WebSocket连接
        registry.addEndpoint("/notify").setAllowedOriginPatterns("*").withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        //基于内存的STOMP消息代理来代替mq的消息代理
        //订阅Broker名称,/user代表点对点即发指定用户，/topic代表发布广播即群发,/equipmentNotify设备相关
        registry.enableSimpleBroker("/user", "/topic");
        //点对点使用的订阅前缀，不设置的话，默认也是/user/
        registry.setApplicationDestinationPrefixes("/app");
    }
}
