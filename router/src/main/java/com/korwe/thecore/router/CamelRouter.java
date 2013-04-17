package com.korwe.thecore.router;

import org.apache.camel.spring.Main;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author <a href="mailto:nithia.govender@korwe.com>Nithia Govender</a>
 */
public class CamelRouter {

    private static final Logger log = LoggerFactory.getLogger(CamelRouter.class);

    public static void main(String[] args) throws Exception {
        log.info("Starting CamelRouter ***");
        AbstractApplicationContext springContext = new ClassPathXmlApplicationContext("/camel-context.xml");
        springContext.registerShutdownHook();
        Main camelMain = new Main();
        camelMain.setApplicationContext(springContext);
        camelMain.run(args);
        log.info("Stopping CamelRouter ***");
    }
}
