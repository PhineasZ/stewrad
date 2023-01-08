package cc.mousse.steward.server.handler;

import cc.mousse.steward.common.Receipt;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import static cc.mousse.steward.common.Constant.*;

/**
 * 回执事件
 *
 * @author PhineasZ
 */
@Slf4j
@Component
@Scope(value = "prototype")
public class ReceiptHandler extends SimpleChannelInboundHandler<Receipt> {

  @Override
  protected void channelRead0(ChannelHandlerContext channelHandlerContext, Receipt receipt) {
    log.debug("Receipt → {}", receipt);
    // 若echo信息匹配，则将回执加入集合
    if (receipt != null) {
      val echo = receipt.getEcho();
      if (echo != null && echo.contains(ECHO_PREFIX)) {
        RECEIPT_MAP.put(echo, receipt);
      }
    }
  }
}
