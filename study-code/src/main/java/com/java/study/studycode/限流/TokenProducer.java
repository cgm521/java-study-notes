package com.java.study.studycode.限流;

/**
 * @Author:wb-cgm503374
 * @Description 令牌生产者，
 * @Date:Created in 2021/4/25 下午11:32
 */

public class TokenProducer implements Runnable {

    private int avgFlowRate;
    private TokenBucket tokenBucket;

    public TokenProducer(int avgFlowRate, TokenBucket tokenBucket) {
        this.avgFlowRate = avgFlowRate;
        this.tokenBucket = tokenBucket;
    }

    @Override
    public void run() {
        tokenBucket.addTokens(avgFlowRate);
    }
}
