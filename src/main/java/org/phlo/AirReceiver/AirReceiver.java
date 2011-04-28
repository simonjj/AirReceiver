package org.phlo.AirReceiver;

import java.io.InputStream;
import java.net.*;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.*;
import java.util.logging.*;

import javax.jmdns.*;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.*;
import org.jboss.netty.channel.socket.nio.*;
import org.jboss.netty.handler.execution.*;

public class AirReceiver {
	private static final Logger s_logger = Logger.getLogger(RtspUnsupportedResponseHandler.class.getName());

	private static final String AirtunesServiceName = "00DEADBEEF00@AirReceiver";
	private static final String AirtunesServiceType = "_raop._tcp.local.";
	private static final short AirtunesServiceRTSPPort = 5000;
	private static final Map<String, String> AirtunesServiceProperties = map(
		"txtvers", "1",
		"tp", "UDP",
		"ch", "2",
		"ss", "16",
		"sr", "44100",
		"pw", "false",
		"sm", "false",
		"sv", "false",
		"ek", "1",
		"et", "0,1",
		"cn", "0,1",
		"vn", "3"
	);
	
    public static final ExecutionHandler ChannelExecutionHandler = new ExecutionHandler(
    	new OrderedMemoryAwareThreadPoolExecutor(4, 0, 0)
    );
	
	private static Map<String, String> map(String... keys_values) {
		assert keys_values.length % 2 == 0;
		Map<String, String> map = new java.util.HashMap<String, String>(keys_values.length / 2);
		for(int i=0; i < keys_values.length; i+=2)
			map.put(keys_values[i], keys_values[i+1]);
		return Collections.unmodifiableMap(map);
	}
	
    public static void main(String[] args) throws Exception {
		final InputStream loggingPropertiesStream =
			AirReceiver.class.getClassLoader().getResourceAsStream("logging.properties");
    	LogManager.getLogManager().readConfiguration(loggingPropertiesStream);
    	
    	/* Register BouncyCaster security provider */
    	java.security.Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
    	
    	/* Get address to listen on */
    	final InetAddress airTunesHostAddress = InetAddress.getLocalHost();

    	/* Create mDNS responder. Also arrange for all services
    	 * to be unregistered on VM shutdown
    	 */
    	final JmDNS jmDns = JmDNS.create(airTunesHostAddress, airTunesHostAddress.getHostName());
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			@Override public void run() {
				jmDns.unregisterAllServices();
	    		s_logger.info("Unregistered all mDNS services");
			}
        }));
        s_logger.info("Created mDNS responder on " + airTunesHostAddress);

        /* Create AirTunes RTSP pipeline factory.
         * NOTE: We immediatly create a test channel. This isn't necessary,
         * but uncoveres failures earlier
         */
        ChannelPipelineFactory airTunesRtspPipelineFactory = new RaopRtspPipelineFactory();
        airTunesRtspPipelineFactory.getPipeline();
        
        /* Create AirTunes RTSP server */
		final ServerBootstrap airTunesRtspBootstrap = new ServerBootstrap(new NioServerSocketChannelFactory(
			Executors.newCachedThreadPool(),
			Executors.newCachedThreadPool()
		));
		airTunesRtspBootstrap.setPipelineFactory(airTunesRtspPipelineFactory);
		airTunesRtspBootstrap.setOption("reuseAddress", true);
		airTunesRtspBootstrap.setOption("child.tcpNoDelay", true);
		airTunesRtspBootstrap.setOption("child.keepAlive", false);
		airTunesRtspBootstrap.bind(new InetSocketAddress(airTunesHostAddress, AirtunesServiceRTSPPort));
        s_logger.info("Launched RTSP service on " + airTunesHostAddress + ":" + AirtunesServiceRTSPPort);
	        
        /* Publish RAOP service */
        final ServiceInfo airTunesServiceInfo = ServiceInfo.create(
    		AirtunesServiceType,
    		AirtunesServiceName,
    		AirtunesServiceRTSPPort,
    		0 /* weight */, 0 /* priority */,
    		AirtunesServiceProperties
    	);
		jmDns.registerService(airTunesServiceInfo);
		s_logger.info("Registered AirTunes service '" + airTunesServiceInfo.getName() + "' on " + airTunesHostAddress);
    }

}
