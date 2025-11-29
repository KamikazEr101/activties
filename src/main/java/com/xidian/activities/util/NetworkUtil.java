package com.xidian.activities.util;

import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

/**
 * 网络工具类
 *
 * @author
 * @since
 */
@Slf4j
public class NetworkUtil {

    /**
     * 获取本机局域网IP地址
     * 优先获取非回环、非虚拟的IPv4地址
     *
     * @return 局域网IP地址，如果获取失败返回null
     */
    public static String getLocalIpAddress() {
        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();

            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = networkInterfaces.nextElement();

                // 跳过回环接口和未激活的接口
                if (networkInterface.isLoopback() || !networkInterface.isUp()) {
                    continue;
                }

                // 跳过虚拟网卡（如VMware、VirtualBox等）
                String displayName = networkInterface.getDisplayName().toLowerCase();
                if (displayName.contains("virtual") || displayName.contains("vmware") ||
                        displayName.contains("virtualbox") || displayName.contains("hyper-v")) {
                    continue;
                }

                Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
                while (inetAddresses.hasMoreElements()) {
                    InetAddress inetAddress = inetAddresses.nextElement();

                    // 只获取IPv4地址，跳过回环地址
                    if (!inetAddress.isLoopbackAddress() && inetAddress.getHostAddress().indexOf(':') == -1) {
                        String ip = inetAddress.getHostAddress();

                        // 优先返回局域网IP（192.168.x.x 或 10.x.x.x）
                        if (ip.startsWith("192.168.") || ip.startsWith("10.")) {
                            log.info("检测到局域网IP地址: {}", ip);
                            return ip;
                        }
                    }
                }
            }

            // 如果没有找到局域网IP，返回第一个非回环的IPv4地址
            networkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = networkInterfaces.nextElement();
                if (networkInterface.isLoopback() || !networkInterface.isUp()) {
                    continue;
                }

                Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
                while (inetAddresses.hasMoreElements()) {
                    InetAddress inetAddress = inetAddresses.nextElement();
                    if (!inetAddress.isLoopbackAddress() && inetAddress.getHostAddress().indexOf(':') == -1) {
                        String ip = inetAddress.getHostAddress();
                        log.info("检测到IP地址: {}", ip);
                        return ip;
                    }
                }
            }

        } catch (Exception e) {
            log.error("获取本机IP地址失败: {}", e.getMessage(), e);
        }

        log.warn("无法获取本机IP地址，将使用localhost");
        return null;
    }

    /**
     * 从URL中提取端口号
     *
     * @param url URL地址（如 http://localhost:9000）
     * @return 端口号，如果没有端口则返回null
     */
    public static String extractPort(String url) {
        if (url == null || url.isEmpty()) {
            return null;
        }

        try {
            // 移除协议前缀
            String urlWithoutProtocol = url.replaceFirst("^https?://", "");

            // 查找端口分隔符
            int portIndex = urlWithoutProtocol.indexOf(':');
            if (portIndex > 0) {
                // 提取端口号（可能包含路径，需要去除）
                String portPart = urlWithoutProtocol.substring(portIndex + 1);
                int pathIndex = portPart.indexOf('/');
                if (pathIndex > 0) {
                    return portPart.substring(0, pathIndex);
                }
                return portPart;
            }
        } catch (Exception e) {
            log.error("从URL提取端口失败: {}", e.getMessage());
        }

        return null;
    }

    /**
     * 构建外部访问URL
     *
     * @param localIp 本机IP
     * @param port    端口号
     * @return 完整的外部访问URL
     */
    public static String buildExternalUrl(String localIp, String port) {
        if (localIp == null || localIp.isEmpty()) {
            return null;
        }

        if (port != null && !port.isEmpty()) {
            return "http://" + localIp + ":" + port;
        } else {
            return "http://" + localIp;
        }
    }
}
