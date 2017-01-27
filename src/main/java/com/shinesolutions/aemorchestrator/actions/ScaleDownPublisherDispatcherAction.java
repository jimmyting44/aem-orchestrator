package com.shinesolutions.aemorchestrator.actions;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.shinesolutions.aemorchestrator.service.AemHelperService;
import com.shinesolutions.aemorchestrator.service.AwsHelperService;

@Component
public class ScaleDownPublisherDispatcherAction implements ScaleAction {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Resource
    private AemHelperService aemHelperService;

    @Resource
    private AwsHelperService awsHelperService;

    public boolean execute(String instanceId) {
        logger.info("ScaleDownPublisherDispatcherAction executing");

        // Find and terminate paired publisher instance
        String pairedPublisherId = aemHelperService.getPublisherIdForPairedDispatcher(instanceId);

        if (pairedPublisherId != null) {
            // Terminate paired publisher
            awsHelperService.terminateInstance(pairedPublisherId);
        } else {
            logger.warn("Unable to located paired publisher instance for publisher dispatcher id: " + instanceId);
        }

        // Change publisher auto scaling group desired capacity to match dispatcher
        int currentDispatcherDesiredCapacity = aemHelperService
            .getAutoScalingGroupDesiredCapacityForPublisherDispatcher();
        int currentPublisherDesiredCapacity = aemHelperService.getAutoScalingGroupDesiredCapacityForPublisher();

        if (currentDispatcherDesiredCapacity == currentPublisherDesiredCapacity) {
            // If desired capacity already the same, then don't do anything
            logger.info("Desired capacity already matching for publisher and dispatcher. No changes will be made");
        } else {
            logger.info("Changing publisher auto scaling group capacity of " + currentPublisherDesiredCapacity + 
                " to match dispatcher's capacity of " + currentDispatcherDesiredCapacity);
            aemHelperService.setAutoScalingGroupDesiredCapacityForPublisher(currentDispatcherDesiredCapacity);
        }
        

        return true;
    }

}