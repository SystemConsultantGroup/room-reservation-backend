package edu.skku.scg.reservation.global.annotation;

import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@PreAuthorize("principal.managingUnitIds != null && !principal.managingUnitIds.isEmpty()")
public @interface AdminApi {
}