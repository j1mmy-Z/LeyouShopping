package com.leyou;

import com.leyou.utils.PayHelper;
import com.leyou.utils.PayState;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = OrderApplication.class)
public class TestPay {
    @Autowired
    private PayHelper payHelper;
    @Test
    public void testWx() {
        String payUrl = payHelper.createPayUrl(1245544534567L,1L,"zyc");
        System.out.println(payUrl);
    }

    @Test
    public void testQueryOrder(){
        PayState payState = payHelper.queryOrder(1255827371895713792L);
        System.out.println(payState.getValue());
    }
}
