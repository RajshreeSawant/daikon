// ============================================================================
//
// Copyright (C) 2006-2017 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.daikon.messages.spring.consumer;

import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.talend.daikon.messages.header.consumer.*;

@Configuration
@AutoConfigureBefore({ DefaultConsumerSettersConfiguration.class })
public class ExecutionContextUpdaterConfiguration {

    @Bean
    public ExecutionContextUpdater executionContextUpdater(CorrelationIdSetter correlationIdSetter, TenantIdSetter tenantIdSetter,
            SecurityTokenSetter securityTokenSetter, UserIdSetter userIdSetter, ServiceAccountIdSetter serviceAccountIdSetter) {
        return new ExecutionContextUpdaterImpl(correlationIdSetter, tenantIdSetter, userIdSetter, securityTokenSetter,
                serviceAccountIdSetter);
    }

}
