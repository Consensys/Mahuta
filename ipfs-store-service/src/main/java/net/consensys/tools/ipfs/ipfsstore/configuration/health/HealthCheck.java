package net.consensys.tools.ipfs.ipfsstore.configuration.health;

import lombok.Data;

public interface HealthCheck {

  /**
   * check if the service is alive
   * @return
   */
  Result check();
  
  /**
   * Result of the health check
   * @author Gregoire Jeanmart <gregoire.jeanmart@consensys.net>
   *
   */
  @Data
  public static class Result {
    private static final Result HEALTHY = new Result(true, null, null);

    public static Result healthy() {
      return HEALTHY;
    }

    public static Result healthy(String message) {
      return new Result(true, message, null);
    }

    public static Result healthy(String message, Object... args) {
      return healthy(String.format(message, args));
    }

    public static Result unhealthy(String message) {
      return new Result(false, message, null);
    }

    public static Result unhealthy(String message, Object... args) {
      return unhealthy(String.format(message, args));
    }

    public static Result unhealthy(Throwable error) {
      return new Result(false, error.getMessage(), error);
    }

    public Result(boolean isHealthy, String message, Throwable error) {
      this.isHealthy = isHealthy;
      this.message = message;
      this.error = error;
    }
    
    private boolean isHealthy;
    private String message;
    private Throwable error;
  }
}
