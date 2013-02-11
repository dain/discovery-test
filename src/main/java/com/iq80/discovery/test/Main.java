package com.iq80.discovery.test;

import com.google.common.util.concurrent.Uninterruptibles;
import com.google.inject.Binder;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Module;
import io.airlift.bootstrap.Bootstrap;
import io.airlift.discovery.client.Announcer;
import io.airlift.discovery.client.DiscoveryModule;
import io.airlift.discovery.client.ServiceDescriptor;
import io.airlift.discovery.client.ServiceSelector;
import io.airlift.jmx.JmxModule;
import io.airlift.json.JsonModule;
import io.airlift.log.LogJmxModule;
import io.airlift.node.NodeModule;
import io.airlift.tracetoken.TraceTokenModule;
import org.weakref.jmx.guice.MBeanModule;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static io.airlift.discovery.client.DiscoveryBinder.discoveryBinder;
import static io.airlift.discovery.client.ServiceAnnouncement.serviceAnnouncement;
import static io.airlift.discovery.client.ServiceTypes.serviceType;

public class Main
{
    public static void main(String[] args)
            throws Exception
    {
        System.setProperty("node.environment", "test");
        System.setProperty("discovery.uri", "http://localhost:8411");

        Bootstrap app = new Bootstrap(
                new NodeModule(),
                new DiscoveryModule(),
                new JsonModule(),
                new MBeanModule(),
                new JmxModule(),
                new LogJmxModule(),
                new TraceTokenModule(),
                new Module()
                {
                    @Override
                    public void configure(Binder binder)
                    {
                        discoveryBinder(binder).bindServiceAnnouncement(serviceAnnouncement("test").build());
                        discoveryBinder(binder).bindSelector("test");
                    }
                });

        Injector injector = app.strictConfig().initialize();
        injector.getInstance(Announcer.class).start();
        ServiceSelector serviceSelector = injector.getInstance(Key.get(ServiceSelector.class, serviceType("test")));

        while (true) {
            System.out.println();
            System.out.println();
            System.out.println("Listing services");
            List<ServiceDescriptor> serviceDescriptors = serviceSelector.selectAllServices();
            for (ServiceDescriptor serviceDescriptor : serviceDescriptors) {
                System.out.println(serviceDescriptor);
            }
            System.out.println("Done");
            Uninterruptibles.sleepUninterruptibly(1, TimeUnit.SECONDS);
        }

    }
}
