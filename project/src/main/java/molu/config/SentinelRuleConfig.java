package molu.config;

import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class SentinelRuleConfig implements InitializingBean {

    @Override
    public void afterPropertiesSet() throws Exception {
        //创建一个规则
        List<FlowRule> rules = new ArrayList<>();
        FlowRule createOrderRule = new FlowRule();
        createOrderRule.setResource("create_short-link");
        //设置限制类型为QPS 每秒查询数
        createOrderRule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        //每秒访问上线
        createOrderRule.setCount(10);
        rules.add(createOrderRule);
        FlowRuleManager.loadRules(rules);
    }
}
