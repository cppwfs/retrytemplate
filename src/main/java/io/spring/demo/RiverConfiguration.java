/*
 * Copyright 2017 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package io.spring.demo;

import java.net.UnknownHostException;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.RetryListener;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.listener.RetryListenerSupport;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

@Configuration
@EnableRetry
public class RiverConfiguration {
	@Bean
	public RetryTemplate retryTemplate() {
		RetryTemplate retryTemplate = new RetryTemplate();

		FixedBackOffPolicy fixedBackOffPolicy = new FixedBackOffPolicy();
		fixedBackOffPolicy.setBackOffPeriod(5000l);
		retryTemplate.setBackOffPolicy(fixedBackOffPolicy);

		SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy();
		retryPolicy.setMaxAttempts(5);
		retryTemplate.setRetryPolicy(retryPolicy);
		retryTemplate.registerListener(listenerSupport());
		return retryTemplate;
	}

	@Bean
	public CommandLineRunner commandLineRunner(RiverInfo riverInfo, RetryTemplate retryTemplate) {
		return new CommandLineRunner() {
			@Override
			public void run(String... args) throws Exception {
				retryTemplate.execute((RetryCallback<Void, UnknownHostException>) retryContext -> {
							riverInfo.retryService();
							return null;
						},
						retryContext -> {
							System.out.println("Well you didn't want to go anyway");
							return null;
						});
			}
		};
	}

	@Bean
	public RetryListener listenerSupport (){
		return new RetryListenerSupport () {
			@Override
			public <T, E extends Throwable> void onError(RetryContext retryContext, RetryCallback<T, E> retryCallback, Throwable throwable) {
				System.out.println("Having some hiccups.  Please be patient");
			}
		};
	}
}
