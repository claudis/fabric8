/**
 * Copyright (C) FuseSource, Inc.
 * http://fusesource.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.fabric8.docker.provider;

import io.fabric8.api.Container;
import io.fabric8.api.ContainerAutoScaler;
import io.fabric8.api.Containers;
import io.fabric8.api.FabricService;
import io.fabric8.api.NameValidator;
import org.fusesource.common.util.Maps;
import org.fusesource.common.util.Strings;
import org.fusesource.common.util.Systems;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 */
public class DockerAutoScaler implements ContainerAutoScaler {
    private static final transient Logger LOG = LoggerFactory.getLogger(DockerAutoScaler.class);

    private final DockerContainerProvider containerProvider;

    public DockerAutoScaler(DockerContainerProvider containerProvider) {
        this.containerProvider = containerProvider;
    }

    @Override
    public void createContainers(String version, String profile, int count) throws Exception {
        CreateDockerContainerOptions.Builder builder = createAuthoScaleOptions();
        if (builder != null) {
            // TODO this is actually generic to all providers! :)
            for (int i = 0; i < count; i++) {
                FabricService fabricService = containerProvider.getFabricService();
                Container[] containers = fabricService.getContainers();
                final CreateDockerContainerOptions.Builder configuredBuilder = builder.number(1).version(version).profiles(profile);

                NameValidator nameValidator = containerProvider.createNameValidator(configuredBuilder.build());
                String name = Containers.createContainerName(containers, profile, containerProvider.getScheme(), nameValidator);

                CreateDockerContainerOptions options = configuredBuilder.name(name).build();
                LOG.info("Creating container name " + name + " version " + version + " profile " + profile + " " + count + " container(s)");
                fabricService.createContainers(options);
            }
        } else {
            LOG.warn("Could not create version " + version + " profile " + profile + " due to missing autoscale configuration");
        }
    }

    protected CreateDockerContainerOptions.Builder createAuthoScaleOptions() {
        CreateDockerContainerOptions.Builder builder = CreateDockerContainerOptions.builder();

/*
        Map<String, ?> properties = containerProvider.getConfiguration();

        String serverUrl = validateProperty(properties,
                "serverUrl",
                DockerContainerProvider.PROPERTY_AUTOSCALE_SERVER_URL,
                "OPENSHIFT_BROKER_HOST",
                OpenShiftConstants.DEFAULT_SERVER_URL);

        String domain = validateProperty(properties,
                "domain",
                DockerContainerProvider.PROPERTY_AUTOSCALE_DOMAIN,
                "OPENSHIFT_NAMESPACE",
                "");

        String login = validateProperty(properties,
                "login",
                DockerContainerProvider.PROPERTY_AUTOSCALE_LOGIN,
                "OPENSHIFT_LOGIN",
                "");

        String password = validateProperty(properties,
                "login",
                DockerContainerProvider.PROPERTY_AUTOSCALE_PASSWORD,
                "OPENSHIFT_PASSWORD",
                "");

        if (Strings.isNotBlank(domain) && Strings.isNotBlank(login) && Strings.isNotBlank(password)) {
            LOG.info("Using serverUrl: " + serverUrl + " domain: " + domain + " login: " + login);
            return builder.serverUrl(serverUrl).domain(domain).login(login).password(password);
        } else {
            return null;
        }
*/
        return builder;
    }


    protected String validateProperty(Map<String, ?> properties, String name, String propertyName, String envVarName, String defaultValue) {
        String answer = Maps.stringValue(properties, propertyName, Systems.getEnvVar(envVarName, defaultValue));
        if (Strings.isNullOrBlank(answer)) {
            LOG.warn("No configured value for " + name + " in property " + propertyName + " or environment variable $" + envVarName);
        }
        return answer;
    }

    @Override
    public void destroyContainers(String profile, int count, List<Container> containers) {
        // TODO

    }
}