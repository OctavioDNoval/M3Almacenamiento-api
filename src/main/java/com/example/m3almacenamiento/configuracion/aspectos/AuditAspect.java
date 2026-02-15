package com.example.m3almacenamiento.configuracion.aspectos;

import com.example.m3almacenamiento.configuracion.anotaciones.SetAuditUser;
import com.example.m3almacenamiento.configuracion.audit.AuditUtils;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class AuditAspect {
    @Autowired
    private AuditUtils auditUtils;

    @Before("@annotation(com.example.m3almacenamiento.configuracion.anotaciones.SetAuditUser)")
    public void setAuditUser(){
        auditUtils.setCurrentUserForAudit();
    }
}
