package net.consensys.mahuta.configuration.health;

public abstract class HealthCheckAbstract implements HealthCheck {

    public abstract Result check();

}
