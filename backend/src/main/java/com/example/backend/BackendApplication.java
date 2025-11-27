package com.example.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 企业报表生成系统启动类
 * 
 * @author Enterprise Report System
 */
@SpringBootApplication
@EnableAsync
@EnableScheduling
public class BackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(BackendApplication.class, args);
        System.out.println("(♥◠‿◠)ﾉﾞ  企业报表生成系统启动成功   ლ(´ڡ`ლ)ﾞ  \n" +
                "                   _ooOoo_                      \n" +
                "                  o8888888o                     \n" +
                "                  88\" . \"88                     \n" +
                "                  (| -_- |)                     \n" +
                "                  O\\  =  /O                     \n" +
                "               ____/`---'\\____                  \n" +
                "             .'  \\\\|     |//  `.                \n" +
                "            /  \\\\|||  :  |||//  \\               \n" +
                "           /  _||||| -:- |||||_  \\              \n" +
                "           |   | \\\\\\  -  /// |   |              \n" +
                "           | \\_|  ''\\---/''  |   |              \n" +
                "           \\  .-\\__  `-`  ___/-. /              \n" +
                "         ___`. .'  /--.--\\  `. . __             \n" +
                "      .\"\" '<  `.___\\_<|>_/___.'  >'\"\".          \n" +
                "     | | :  `- \\`.;`\\ _ /`;.`/ - ` : | |        \n" +
                "     \\  \\ `-.   \\_ __\\ /__ _/   .-` /  /        \n" +
                "======`-.____`-.___\\_____/___.-`____.-'======   \n" +
                "                   `=---='                      \n" +
                "^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^ \n" +
                "          佛祖保佑       永无BUG                  \n" +
                "  API文档: http://localhost:8080/api/doc.html   ");
    }

}
