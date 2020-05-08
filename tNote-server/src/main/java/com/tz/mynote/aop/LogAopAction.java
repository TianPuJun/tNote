package com.tz.mynote.aop;

import com.tz.mynote.annotation.OptionalLog;
import com.tz.mynote.note.bean.NoteLog;
import com.tz.mynote.note.service.LogService;
import com.tz.mynote.util.GsonUtil;
import com.tz.mynote.util.LoginInfoUtil;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author tz
 * @Classname LogAopAction
 * @Description apo log
 * @Date 2019-09-06 09:22
 */
@Component
@Aspect
public class LogAopAction {
    /**
     *     注入service,用来将日志信息保存在数据库
      */
    @Autowired
    private LogService logService;

    // 配置接入点，即为所要记录的action操作目录
//    @Pointcut("execution(public * com.tz.mynote.note.controller..*.*(..))")
    @Pointcut("@annotation(com.tz.mynote.annotation.OptionalLog)")
    private void controllerAspect() {
    }

    @Around("controllerAspect()")
    public Object around(ProceedingJoinPoint pjp) throws Throwable {
        //日志实体对象
        NoteLog noteLog = new NoteLog();
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                .getRequest();
        // 从session获取用户名
        String username =(String) request.getAttribute("user");
//        String username = (String)request.getSession().getAttribute("user");
        if(null == username){
            noteLog.setUserName("未查找到用户名");
        }else{
            noteLog.setUserName(username);
        }
        // 获取系统当前时间
        noteLog.setGmtCreate(new Date());
        // 获取访问真实IP
        String ipAddress = LoginInfoUtil.getRequestIp(request);
//        String address = LoginInfoUtil.getAddress(ipAddress);
        noteLog.setActionAddress("address todo 后续使用");
        noteLog.setIp(ipAddress);
        // 拦截的实体类，就是当前正在执行的controller
        Object target = pjp.getTarget();
        // 拦截的方法名称。当前正在执行的方法
        String methodName = pjp.getSignature().getName();
        // 拦截的方法参数
        Object[] args = pjp.getArgs();
        //获取请求路径
        String actionUrl = request.getRequestURI();
        // 拦截的放参数类型
        Signature sig = pjp.getSignature();
        MethodSignature msig = null;
        if (!(sig instanceof MethodSignature)) {
            throw new IllegalArgumentException("该注解只能用于方法");
        }
        msig = (MethodSignature) sig;
        Class[] parameterTypes = msig.getMethod().getParameterTypes();
        Object object = null;
        // 获得被拦截的方法
        Method method = null;
        try {
            method = target.getClass().getMethod(methodName, parameterTypes);
        } catch (NoSuchMethodException e1) {
            e1.printStackTrace();
        } catch (SecurityException e1) {
            e1.printStackTrace();
        }
        if (null != method) {
            // 获取方法（此为自定义注解）
            if (method.isAnnotationPresent(OptionalLog.class)) {
                OptionalLog op = method.getAnnotation(OptionalLog.class);

                // 获取注解的modules 设为操作模块
                noteLog.setModule(op.module());
                // 获取注解的methods 设为执行方法
                noteLog.setMethods(op.methods());
                // 将上面获取到的请求路径 设为请求路径
                noteLog.setActionUrl(actionUrl);
                try {
                    object = pjp.proceed();
                    //接受客户端的数据
                    Map<String, String[]> map = request.getParameterMap();
                    // 解决获取参数乱码
                    Map<String, String[]> newmap = new HashMap<String, String[]>(16);
                    for (Map.Entry<String, String[]> entry : map.entrySet()) {
                        String name = entry.getKey();
                        String[] values = entry.getValue();

                        if (values == null) {
                            newmap.put(name, new String[]{});
                            continue;
                        }
                        String[] newValues = new String[values.length];
                        for (int i = 0; i < values.length; i++) {
                            String value = values[i];
                            value = new String(value.getBytes("iso8859-1"), request.getCharacterEncoding());
                            //解决乱码后封装到Map中
                            newValues[i] = value;
                        }

                        newmap.put(name, newValues);

                    }
                    noteLog.setContent(GsonUtil.toJson(newmap));
                    //1为执行成功
                    noteLog.setCommit((byte) 1);
                    // 添加到数据库
                    logService.save(noteLog);
                } catch (Throwable e) {
                    //接受客户端的数据
                    Map<String, String[]> map = request.getParameterMap();
                    // 解决获取参数乱码
                    Map<String, String[]> newmap = new HashMap<String, String[]>(16);
                    for (Map.Entry<String, String[]> entry : map.entrySet()) {
                        String name = entry.getKey();
                        String[] values = entry.getValue();

                        if (values == null) {
                            newmap.put(name, new String[]{});
                            continue;
                        }
                        String[] newvalues = new String[values.length];
                        for (int i = 0; i < values.length; i++) {
                            String value = values[i];
                            value = new String(value.getBytes("iso8859-1"), request.getCharacterEncoding());
                            newvalues[i] = value;
                            //解决乱码后封装到Map中
                        }
                        newmap.put(name, newvalues);
                    }
                    noteLog.setContent(GsonUtil.toJson(newmap));
                    //2为执行失败
                    noteLog.setCommit((byte) 2);
                    //添加到数据库
                    logService.save(noteLog);
                }
            }
        }
        return object;
    }
}
