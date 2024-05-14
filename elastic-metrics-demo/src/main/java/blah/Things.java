package blah;

public class Things {

  public record LogEntry(String hostIp, String message) {
  };

  public record IpLocation(String ip, String country, String city) {
  };
}
