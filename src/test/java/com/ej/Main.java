package com.ej;

import com.ej.chain.dto.BaseResponse;
import com.ej.chain.proxy.ProxyHandlerFactory;
import com.ej.credit.dto.CreditRequest;
import com.ej.credit.dto.CreditResponse;
import com.ej.credit.handlers.CreditApplyHandler;
import com.ej.credit.handlers.CreditFinalHandler;
import com.ej.credit.handlers.CreditParamsCheckHandler;
import com.ej.manage.EjManage;

import java.math.BigDecimal;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {

    public static void thread() throws InterruptedException {
        int num = 20;
        ExecutorService executorService = Executors.newFixedThreadPool(8);
        final CountDownLatch countDownLatch = new CountDownLatch(num);
        EjManage<CreditRequest, CreditResponse> ejManage = new EjManage<>();
        ejManage.register(new CreditParamsCheckHandler());
        ejManage.register(ProxyHandlerFactory.getJavassistProxyHandlerInstance(CreditApplyHandler.class,true));
        ejManage.register(ProxyHandlerFactory.getJavassistProxyHandlerInstance(CreditFinalHandler.class,true));
        for (int idx = 0; idx < num; idx++) {
            final String applyNo = String.valueOf(idx);
            executorService.execute(() -> {
                CreditRequest creditRequest = new CreditRequest();
                creditRequest.setProductCode("XY");
                creditRequest.setApplyNo(applyNo);
                creditRequest.setApplyAmount(new BigDecimal(500));
                BaseResponse<CreditResponse> baseResponse = ejManage.execute(creditRequest);
                System.out.println(baseResponse);
                countDownLatch.countDown();
            });
        }
        countDownLatch.await();
        System.gc();
        Thread.sleep(1000);
        System.gc();
        executorService.shutdown();
    }

    public static void main(String[] args) throws InterruptedException {
        int times = 1;

        CreditRequest creditRequest = new CreditRequest();
        creditRequest.setProductCode("XY");
        creditRequest.setApplyNo("9");
        creditRequest.setApplyAmount(new BigDecimal(500));

        EjManage<CreditRequest, CreditResponse> javassistEjManage = new EjManage<>();
        javassistEjManage.register(new CreditParamsCheckHandler());
        javassistEjManage.register(ProxyHandlerFactory.getJavassistProxyHandlerInstance(CreditApplyHandler.class, true));
        javassistEjManage.register(ProxyHandlerFactory.getJavassistProxyHandlerInstance(CreditFinalHandler.class, true));

        long s = System.currentTimeMillis();
        test(times, javassistEjManage, creditRequest);
        System.out.println(System.currentTimeMillis() - s);

    }

    public static void test(int times, EjManage<CreditRequest, CreditResponse> ejManage, CreditRequest creditRequest) {
        for (int idx = 0; idx < times; idx++) {
            BaseResponse<CreditResponse> baseResponse = ejManage.execute(creditRequest);
        }
    }
}
