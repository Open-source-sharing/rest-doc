package smartdoc.client.springmvc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.PriorityOrdered;
import restdoc.client.api.Agent;
import restdoc.client.api.AgentClientConfiguration;
import restdoc.client.api.AgentImpl;
import restdoc.remoting.netty.NettyRequestProcessor;
import smartdoc.client.springmvc.handler.ExportApiHandler;
import smartdoc.client.springmvc.handler.InvokerApiHandler;
import smartdoc.client.springmvc.handler.ReportClientInfoHandler;


@Configuration
@Import(value = {EnvConfiguration.class})
@AutoConfigureBefore(value = {EnvConfiguration.class})
public class SpringMVCAgentClientConfiguration
    implements AgentClientConfiguration, PriorityOrdered, CommandLineRunner {

  private final AgentImpl agentImpl;

  private final ReportClientInfoHandler reportClientInfoHandler;

  private final ExportApiHandler exportApiHandler;

  private final InvokerApiHandler invokerApiHandler;

  @Autowired
  public SpringMVCAgentClientConfiguration(
      AgentImpl agentImpl,
      ReportClientInfoHandler reportClientInfoHandler,
      ExportApiHandler exportApiHandler,
      InvokerApiHandler invokerApiHandler) {
    this.agentImpl = agentImpl;
    this.reportClientInfoHandler = reportClientInfoHandler;
    this.exportApiHandler = exportApiHandler;
    this.invokerApiHandler = invokerApiHandler;
  }

  @Override
  public void run(String... args) throws Exception {
    start();
  }

  @Override
  public int getOrder() {
    return 0;
  }

  @Override
  public NettyRequestProcessor getInvokeAPIHandler() {
    return this.invokerApiHandler;
  }

  @Override
  public NettyRequestProcessor getReportClientInfoHandler() {
    return this.reportClientInfoHandler;
  }

  @Override
  public NettyRequestProcessor getExportAPIHandler() {
    return this.exportApiHandler;
  }

  @Override
  public Agent getAgent() {
    return this.agentImpl;
  }

  @Override
  public String module() {
    return "SpringMVC-module";
  }
}
