package org.keyus.study.my.core.servlet;

import org.keyus.study.my.core.annotation.*;
import org.keyus.study.my.core.uitl.StringTool;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;

/**
 * @author keyus
 * @version 1.0
 * 假设MyDispatcherServlet类中所有的集合对象在创建完成之后仅存在读的
 * 操作,暂时不考虑并发下的线程安全问题
 */
public class MyDispatcherServlet extends HttpServlet {

    // 读取配置
    private Properties properties = new Properties();

    // 类的全路径集合(用于反射创建实例对象)
    private List<String> classNames = new ArrayList<>();

    // IOC容器
    private Map<String, Object> ioc = new HashMap<>();

    // url与method的映射关系
    private Map<String, Method> handlerMappings = new HashMap<>();

    // url与controller的映射关系
    private Map<String, Object> controllerMappings = new HashMap<>();

    @Override
    public void init() throws ServletException {
        // 1.读取配置文件的信息
        doLoadConfig(getInitParameter("contextConfigLocation"));
        // 2.扫描包
        doScanner(properties.getProperty("scanPackage"));
        // 3.通过反射机制实例化扫描到的类的对象
        doInstance();
        // 4.初始化url与method的映射关系
        initHandlerMapping();
        // 5.实现依赖注入(控制反转)
        doIoc();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doDispatcher(request, response);
    }

    @Override
    public void destroy() {
        super.destroy();
    }

    // 获取配置文件数据
    private void doLoadConfig(String location){
        if (location.startsWith("classpath:"))
            location = location.replace("classpath:", "");
        else if (location.contains("/"))
            location = location.substring(location.lastIndexOf('/') + 1);
        try (InputStream is = getClass().getClassLoader()
                .getResourceAsStream(location)){
            properties.load(is);
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    // 扫描包
    private void doScanner(String packageName){
        URL url = getClass().getClassLoader().getResource("/" + packageName
                .replaceAll("\\.", "/"));
        if (url.getFile() != null){
            String fileStr = url.getFile();
            File dir = new File(fileStr);
            if (dir.list() != null){
                for (String path : dir.list()){
                    File filePath = new File(fileStr + path);
                    if (filePath.isDirectory())
                        doScanner(packageName + "." + path);
                    else
                        classNames.add(packageName + "." + filePath.getName().replace(".class", ""));
                }
            }
        }
    }

    // 初始化容器中所有的实例
    private void doInstance(){
        if (classNames.isEmpty())
            return;
        for (String className : classNames){
            try {
                // 反射得到Class对象
                Class<?> clazz = Class.forName(className);
                // 如果该类标注了MyController注解
                if (clazz.isAnnotationPresent(MyController.class)){
                    // 获取该类在IOC容器中被定义的名字(key)
                    String key = clazz.getAnnotation(MyController.class).value();
                    if (!"".equals(key))
                        // 用该类在IOC容器中被定义的名字存放一个该类的实例(单例模式)
                        ioc.put(key, clazz.newInstance());
                    else
                        // 如果没有定义名字,则同spring一样,按照类名作为key(首字母小写)
                        ioc.put(StringTool.toLowerFirstWord(clazz.getSimpleName()), clazz.newInstance());
                } else if (clazz.isAnnotationPresent(MyService.class)){
                    // 获取该类在IOC容器中被定义的名字(key)
                    String key = clazz.getAnnotation(MyService.class).value();
                    if (!"".equals(key))
                        // 用该类在IOC容器中被定义的名字存放一个该类的实例(单例模式)
                        ioc.put(key, clazz.newInstance());
                    else
                        // 如果没有定义名字,则同spring一样,按照类名作为key(首字母小写)
                        ioc.put(StringTool.toLowerFirstWord(clazz.getSimpleName()), clazz.newInstance());
                } else if (clazz.isAnnotationPresent(MyRepository.class)){
                    // 获取该类在IOC容器中被定义的名字(key)
                    String key = clazz.getAnnotation(MyRepository.class).value();
                    if (!"".equals(key))
                        // 用该类在IOC容器中被定义的名字存放一个该类的实例(单例模式)
                        ioc.put(key, clazz.newInstance());
                    else
                        // 如果没有定义名字,则同spring一样,按照类名作为key(首字母小写)
                        ioc.put(StringTool.toLowerFirstWord(clazz.getSimpleName()), clazz.newInstance());
                }
            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    // 初始化URL与方法和Controller间的映射关系
    private void initHandlerMapping(){
        if (ioc.isEmpty())
            return;
        try {
            for (Map.Entry<String, Object> entry : ioc.entrySet()){
                Class<?> clazz = entry.getValue().getClass();
                // 找出标注MyController的类
                if (clazz.isAnnotationPresent(MyController.class)){
                    // baseUrl表示Controller类标注的URL
                    String baseUrl = "";
                    Object controller = entry.getValue();
                    if (clazz.isAnnotationPresent(MyRequestMapping.class))
                        baseUrl = clazz.getAnnotation(MyRequestMapping.class).value();
                    Method[] methods = clazz.getMethods();
                    for (Method method : methods){
                        if (method.isAnnotationPresent(MyRequestMapping.class)){
                            String url = (baseUrl + '/' + method
                                    .getAnnotation(MyRequestMapping.class).value())
                                    .replaceAll("/+", "/");
                            // 放入两个映射关系表中
                            handlerMappings.put(url, method);
                            controllerMappings.put(url, controller);
                        }
                    }
                }
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    // 进行依赖注入
    private void doIoc(){
        if (ioc.isEmpty())
            return;
        for (Map.Entry<String, Object> entry : ioc.entrySet()){
            Object instance = entry.getValue();
            // 获取当前实例的Class对象
            Class<?> clazz = instance.getClass();
            if (clazz.isAnnotationPresent(MyController.class)){
                // 获取所有该Class声明的属性
                Field[] fields = clazz.getDeclaredFields();
                for (Field field : fields){
                    if (field.isAnnotationPresent(MyAutowired.class)){
                        MyAutowired autowired = field.getAnnotation(MyAutowired.class);
                        String key = autowired.value();
                        if ("".equals(key)){
                              // 有误的代码段,待完善
//                            // 获取当前字段的Class对象
//                            Class<? extends Field> fieldClass = field.getClass();
//                            for (Object object : ioc.values()){
//                                if (object.getClass() == fieldClass) {
//                                    // 进行属性的反射注入
//                                    field.setAccessible(true);
//                                    try {
//                                        field.set(instance, ioc.get(key));
//                                    } catch (IllegalAccessException e) {
//                                        e.printStackTrace();
//                                    }
//                                }
//                            }
                        } else {
                            // 进行属性的反射注入
                            field.setAccessible(true);
                            try {
                                field.set(instance, ioc.get(key));
                            } catch (IllegalAccessException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        }
    }

    // 处理方法调度
    private void doDispatcher(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (handlerMappings.isEmpty())
            return;
        String url = request.getRequestURI();
        String contextPath = request.getContextPath();
        // 替换URL值的多个/为当/
        url = url.replace(contextPath, "").replaceAll("/+", "/");

        if (!handlerMappings.containsKey(url)){
            response.getWriter().write("404 NOT FOUND!");
            return;
        }

        // 获得待执行的方法
        Method method = handlerMappings.get(url);
        // 获得该方法的参数列表
        Class<?>[] parameterTypes = method.getParameterTypes();
        // 保存参数值
        Object[] parameterValues = new Object[parameterTypes.length];

        // 处理调用方法的参数(可以采用策略模式改进)
        int argsIndex = 0;
        int index = 0;
        for (Class<?> paramClazz : parameterTypes){
            // 处理request
            if (ServletRequest.class.isAssignableFrom(paramClazz)){
                parameterValues[argsIndex++] = request;
            }
            // 处理response
            if (ServletResponse.class.isAssignableFrom(paramClazz)){
                parameterValues[argsIndex++] = response;
            }
            Annotation[] parameterAnnotations = method.getParameterAnnotations()[index];
            // 处理标注了MyRequestParam的参数
            if (parameterAnnotations.length > 0){
                for (Annotation annotation : parameterAnnotations){
                    if (MyRequestParam.class.isAssignableFrom(annotation.getClass())){
                        MyRequestParam rp = (MyRequestParam) annotation;
                        parameterValues[argsIndex++] = request.getParameter(rp.value());
                    }
                }
            }
            index++;
        }

        // 反射调用Controller实例的方法
        try {
            method.invoke(controllerMappings.get(url), parameterValues);
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
