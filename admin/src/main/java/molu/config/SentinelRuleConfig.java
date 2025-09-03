package molu.config;

import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class SentinelRuleConfig implements InitializingBean {

    @Override
    public void afterPropertiesSet() {
        List<FlowRule> flowRules = new ArrayList<>();
        FlowRule flowRule = new FlowRule();
        flowRule.setResource("userRegister");
        flowRule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        flowRule.setCount(20); // 每秒100个请求
        flowRules.add(flowRule);
        FlowRuleManager.loadRules(flowRules);

        // 熔断规则：当限流频繁触发时启动熔断
        List<DegradeRule> degradeRules = new ArrayList<>();
        DegradeRule degradeRule = new DegradeRule();
        degradeRule.setResource("userRegister");
        degradeRule.setGrade(RuleConstant.DEGRADE_GRADE_EXCEPTION_COUNT);
        degradeRule.setCount(10); // 限流触发50次后熔断
        degradeRule.setTimeWindow(30); // 熔断30秒
        degradeRules.add(degradeRule);
        DegradeRuleManager.loadRules(degradeRules);
    }
}
