package restdoc.client.dubbo.test.controller;

import java.util.HashMap;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Overman
 * @since 2020/9/27
 */
@RestController
public class EchoHttpController1 {

  @GetMapping(value = "/echo1")
  public Object echoRestWeb() {
    Map<String, Object> map = new HashMap<>();
    map.put("applicationType", "DUBBO");
    map.put("msg", "欢迎测试Http服务");
    return map;
  }
}
