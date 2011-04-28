package org.phlo.AirReceiver;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;

public class RtpLoggingHandler extends SimpleChannelHandler {
	private static final Logger s_logger = Logger.getLogger(RtpLoggingHandler.class.getName());

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent evt)
		throws Exception
	{
		RtpPacket packet = (RtpPacket)evt.getMessage();
		
		Level level = (packet.getPayloadType() == RaopRtpPacket.Audio.PayloadType) ? Level.FINEST : Level.FINE;
		if (s_logger.isLoggable(level))
			s_logger.log(level, evt.getRemoteAddress() + "> " + packet.toString());
		
		super.messageReceived(ctx, evt);
	}
	
	@Override
	public void writeRequested(ChannelHandlerContext ctx, MessageEvent evt)
		throws Exception
	{
		RtpPacket packet = (RtpPacket)evt.getMessage();
		
		Level level = (packet.getPayloadType() == RaopRtpPacket.Audio.PayloadType) ? Level.FINEST : Level.FINE;
		if (s_logger.isLoggable(level))
			s_logger.log(level, evt.getRemoteAddress() + "< " + packet.toString());
		
		super.writeRequested(ctx, evt);
	}
}
