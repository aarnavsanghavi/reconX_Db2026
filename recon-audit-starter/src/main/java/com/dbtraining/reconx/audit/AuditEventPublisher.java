package com.dbtraining.reconx.audit;

import org.springframework.context.ApplicationEventPublisher;

public class AuditEventPublisher {
    private final ApplicationEventPublisher publisher;
    private final AuditProperties props;

    public AuditEventPublisher(ApplicationEventPublisher publisher, AuditProperties props) {
        this.publisher = publisher;
        this.props = props;
    }

    public void publish(String message) {
        System.out.println("Audit event [" + props.getTopic() + "]: " + message);
    }
}
