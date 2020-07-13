package org.bndly.examples.rest.resource;

/*-
 * #%L
 * Bndly examples REST HATEOAS
 * %%
 * Copyright (C) 2020 Cybercon GmbH
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import org.bndly.examples.rest.resource.beans.BundleListRestBean;
import org.bndly.examples.rest.resource.beans.BundleReferenceRestBean;
import org.bndly.examples.rest.resource.beans.BundleRestBean;
import org.bndly.examples.rest.resource.beans.ServiceListRestBean;
import org.bndly.examples.rest.resource.beans.ServiceReferenceRestBean;
import org.bndly.examples.rest.resource.beans.ServiceRestBean;
import org.bndly.rest.api.Resource;
import org.bndly.rest.api.ResourceProvider;
import org.bndly.rest.api.ResourceURI;
import org.bndly.rest.atomlink.api.annotation.AtomLink;
import org.bndly.rest.atomlink.api.annotation.AtomLinks;
import org.bndly.rest.atomlink.api.annotation.Parameter;
import org.bndly.rest.common.beans.Services;
import org.bndly.rest.common.beans.error.ErrorRestBean;
import org.bndly.rest.controller.api.ControllerResourceRegistry;
import org.bndly.rest.controller.api.GET;
import org.bndly.rest.controller.api.POST;
import org.bndly.rest.controller.api.Path;
import org.bndly.rest.controller.api.PathParam;
import org.bndly.rest.controller.api.Response;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component(service = { OSGIServiceResource.class}, immediate = true)
@Path("felix")
public class OSGIServiceResource implements Resource {

    private static Logger log = LoggerFactory.getLogger(OSGIServiceResource.class);

    @Reference
    private ControllerResourceRegistry controllerResourceRegistry;

    private BundleContext bundleContext;

    @Activate
    public void activate(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
        controllerResourceRegistry.deploy(this);
    }

    @Override
    public ResourceURI getURI() {
        return null;
    }

    @Override
    public ResourceProvider getProvider() {
        return null;
    }

    @GET
    @AtomLinks({
            @AtomLink(rel = "bundles", target = Services.class),
            @AtomLink(target = BundleListRestBean.class)
    })
    @Path("bundles")
    public Response listBundles() {
        Bundle[] allBundles = bundleContext.getBundles();
        BundleListRestBean bundleListRestBean = new BundleListRestBean();
        bundleListRestBean.setItems(new ArrayList<>(createBundleStructure(allBundles)));
        return Response.ok(bundleListRestBean);
    }

    @GET
    @AtomLinks({
            @AtomLink(rel = "services", target = Services.class),
            @AtomLink(target = ServiceListRestBean.class)
    })
    @Path("services")
    public Response listServices(){
        ServiceListRestBean serviceList = new ServiceListRestBean();
        try {
            ServiceReference<?>[] allServices = bundleContext.getAllServiceReferences(null, null);
            serviceList.getItems().addAll(createServiceStructure(allServices));
        } catch (InvalidSyntaxException e) {
            return error("Could not obtain service list", e);
        }
        return Response.ok(serviceList);
    }

    @GET
    @Path("service/{serviceId}")
    @AtomLinks({
            @AtomLink(rel = "self", target = ServiceRestBean.class, parameters = @Parameter(name = "serviceId", expression = "${this.serviceId}")),
            @AtomLink(target = ServiceListRestBean.class, parameters = @Parameter(name = "serviceId", expression = "${this.serviceId}"))
    })
    public Response getService(@PathParam("serviceId") Long serviceId) {
        try {
            ServiceReference<?>[] foundServiceRefs = bundleContext.getAllServiceReferences(null, "(service.id=" + serviceId + ")" );
            Collection<ServiceRestBean> services = createServiceStructure(foundServiceRefs);
            if(services.isEmpty()) {
                return error("Found no service with id = "+serviceId);
            }
            if(services.size() != 1) {
                return error("Got more than one service with id = "+serviceId);
            }
            return Response.ok(services.iterator().next());
        } catch (InvalidSyntaxException e) {
            return error("Could not get service.");
        }
    }

    @GET
    @Path("bundle/{bundleId}")
    @AtomLinks({
            @AtomLink(rel = "self", target = BundleRestBean.class, parameters = @Parameter(name = "bundleId", expression = "${this.bundleId}")),
            @AtomLink(target = BundleListRestBean.class, parameters = @Parameter(name = "bundleId", expression = "${this.bundleId}"))
    })
    public Response getBundle(@PathParam("bundleId") Long bundleId){
        Bundle bundle = bundleContext.getBundle(bundleId);
        if(bundle != null) {
            Bundle [] ary = { bundle };
            Collection<BundleReferenceRestBean> foundBundles = createBundleStructure(ary);
            if(foundBundles.size() != 1){
                return error("Unexpected count of bundles found.");
            }
            return Response.ok(foundBundles.iterator().next());
        }
        return error("Could not load bundle with id = "+bundleId);
    }

    @POST
    @Path("bundle/{bundleId}/stop")
    @AtomLinks({
            @AtomLink(rel = "stop", target = BundleRestBean.class, parameters = @Parameter(name = "bundleId", expression = "${this.bundleId}")),
    })
    public Response stopBundle(@PathParam("bundleId") long bundleId){
        Bundle bundle = bundleContext.getBundle(bundleId);
        if (bundle != null) {
            try {
                bundle.stop();
            } catch (BundleException e) {
                return error("Could not stop bundle.",e);
            }
        }
        return Response.NO_CONTENT;
    }

    // Here we create structures of Bundles having Services having a Bundle having Services....
    // The reason is to have an object graph which can't be simply serialized because of circular dependencies
    // when the response of a service is processed, cycles are replaced with references to objects which had been
    // submitted earlier in the response. This is done by the cycle-breaker

    private Collection<ServiceRestBean> createServiceStructure(ServiceReference<?>[] services){

        Map<Long, BundleRestBean> bundleMap = new HashMap<>();
        Map<Long, ServiceRestBean> serviceMap = new HashMap<>();

        Arrays.asList(services).stream().forEach(service -> {

            ServiceRestBean serviceRestBean = mapService(service);
            serviceMap.put(serviceRestBean.getServiceId(), serviceRestBean);

            Bundle bundle = service.getBundle();
            BundleRestBean bundleRestBean = bundleMap.get(bundle.getBundleId());
            if (bundleRestBean == null) {
                bundleRestBean = mapBundle(bundle);
                bundleMap.put(bundleRestBean.getBundleId(), bundleRestBean);
            }
            bundleRestBean.getServices().add(serviceRestBean);
            serviceRestBean.setBundle(bundleRestBean);

        });
        return serviceMap.values();
    }

    private Collection<BundleReferenceRestBean> createBundleStructure(Bundle [] bundles) {

        Map<Long, BundleReferenceRestBean> bundleMap = new HashMap<>();
        Map<Long, ServiceRestBean> serviceMap = new HashMap<>();

        Arrays.asList(bundles).stream().forEach( bundle -> {

            BundleRestBean bundleRestBean = mapBundle(bundle);
            bundleMap.put(bundleRestBean.getBundleId(), bundleRestBean);

            ServiceReference<?>[] registeredServices = bundle.getRegisteredServices();
            if ((registeredServices != null) && (registeredServices.length > 0)) {

                List<ServiceReferenceRestBean> bundleServices = new ArrayList<>();
                Arrays.asList(registeredServices).stream().forEach(registeredService -> {
                    Long serviceId = Long.valueOf(registeredService.getProperty("service.id").toString());
                    ServiceRestBean serviceRestBean = serviceMap.get(serviceId);
                    if (serviceRestBean == null) {
                        serviceRestBean = mapService(registeredService);
                        serviceRestBean.setBundle(bundleRestBean);
                        serviceMap.put(serviceId, serviceRestBean);
                    }
                    bundleServices.add(serviceRestBean);
                });
                bundleRestBean.setServices(bundleServices);
            }
        });

        return bundleMap.values();
    }

    private BundleRestBean mapBundle(Bundle bundle){
        BundleRestBean bundleRestBean = new BundleRestBean();
        bundleRestBean.setBundleId(bundle.getBundleId());
        bundleRestBean.setName(bundle.getSymbolicName());
        return bundleRestBean;
    }

    private ServiceRestBean mapService(ServiceReference ref){
        ServiceRestBean serviceRestBean = new ServiceRestBean();
        Object sId = ref.getProperty("service.id");
        serviceRestBean.setServiceId(Long.valueOf(sId.toString()));
        Object componentName = ref.getProperty("component.name");
        serviceRestBean.setName(componentName != null ? componentName.toString() : "unnamed");
        return serviceRestBean;
    }

    private Response error(String message){
        return error(message, null);
    }

    private Response error(String message, Exception e){
        log.error(message,e);
        ErrorRestBean errorBean = new ErrorRestBean();
        errorBean.setMessage(message);
        return Response.status(400).entity(errorBean);
    }

}
