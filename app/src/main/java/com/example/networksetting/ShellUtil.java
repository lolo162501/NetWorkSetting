package com.example.networksetting;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class ShellUtil {
    public static final String COMMAND_SU = "su";
    public static final String COMMAND_SH = "sh";
    public static final String COMMAND_EXIT = "exit\n";
    public static final String COMMAND_LINE_END = "\n";

    public static boolean checkRootPermission() {
        return execCommand("echo root", true, false).result == 0;
    }

//        String s = "ifconfig eth0 " + getIpAddressString() + " netmask " + getMaskString();
//        System.out.println("onClick: ----------------------" + s);
//        ShellUtil.execCommand("ifconfig eth0 " + getIpAddressString() + " netmask " + getMaskString(), true, true);
//        ShellUtil.execCommand("route add default gw " + getGateWayString() + " dev eth0", true, true);
//        ShellUtil.execCommand("setprop net.eth0.dns0 " + getDNS1String(), true, true);
//        ShellUtil.execCommand("setprop net.eth0.dns1 " + getDNS2String(), true, true);

//    public static CommandResult execCommand(String command, boolean isRoot) {
//        return execCommand(new String[]{command}, isRoot, true);
//    }

//    public static CommandResult execCommand(List<String> commands, boolean isRoot) {
//        return execCommand(commands == null ? null : commands.toArray(new String[]{}),
//                isRoot, true);
//    }

//    public static CommandResult execCommand(String[] commands, boolean isRoot) {
//        return execCommand(commands, isRoot, true);
//    }

    public static CommandResult execCommand(String command, boolean isRoot, boolean isNeedResultMsg) {
        return execCommand(new String[]{command}, isRoot, isNeedResultMsg);
    }

    public static CommandResult execCommand(String[] commands, boolean isRoot, boolean isNeedResultMsg) {
        int result = -1;
        if (commands == null || commands.length == 0) {
            return new CommandResult(result, null, null);
        }
        Process process = null;
        BufferedReader successResult = null;
        BufferedReader errorResult = null;
        StringBuilder successMessage = null;
        StringBuilder errorMessage = null;
        DataOutputStream dataOutputStream = null;
        try {
            process = Runtime.getRuntime().exec(isRoot ? COMMAND_SU : COMMAND_SH);
            dataOutputStream = new DataOutputStream(process.getOutputStream());
            for (String command : commands) {
                if (command == null) {
                    continue;
                }
                dataOutputStream.write(command.getBytes());
                dataOutputStream.writeBytes(COMMAND_LINE_END);
                dataOutputStream.flush();
            }
            dataOutputStream.writeBytes(COMMAND_EXIT);
            dataOutputStream.flush();
            result = process.waitFor();
            // get command result
            if (isNeedResultMsg) {
                successMessage = new StringBuilder();
                errorMessage = new StringBuilder();
                successResult = new BufferedReader(new InputStreamReader(process.getInputStream()));
                errorResult = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                String string;
                while ((string = successResult.readLine()) != null) {
                    successMessage.append(string);
                }
                while ((string = errorResult.readLine()) != null) {
                    errorMessage.append(string);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (dataOutputStream != null) {
                    dataOutputStream.close();
                }
                if (successResult != null) {
                    successResult.close();
                }
                if (errorResult != null) {
                    errorResult.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (process != null) {
                process.destroy();
            }
        }
        return new CommandResult(
                result,
                successMessage == null ? null : successMessage.toString(),
                errorMessage == null ? null : errorMessage.toString());
    }

//    public static CommandResult execCommand(List<String> commands, boolean isRoot, boolean isNeedResultMsg) {
//        return execCommand(commands == null ? null : commands.toArray(new String[]{}),
//                isRoot, isNeedResultMsg);
//    }

    public static class CommandResult {
        public int result;
        public String successMsg;
        public String errorMsg;

        public CommandResult(int result) {
            this.result = result;
        }

        public CommandResult(int result, String successMsg, String errorMsg) {
            this.result = result;
            this.successMsg = successMsg;
            this.errorMsg = errorMsg;
        }
    }
}

