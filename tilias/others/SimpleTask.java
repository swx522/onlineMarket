//李宇轩 2353737
package com.garfield.commands;

import com.garfield.service.PomodoroTimer;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(
    name = "timer",
    description = "启动Pomodoro计时器"
)
public class TimerCommand implements Runnable {
    
    @Option(names = {"--work"}, 
            description = "工作时间（秒）", 
            defaultValue = "25")
    private int workSeconds;
    
    @Option(names = {"--break"}, 
            description = "休息时间（秒）", 
            defaultValue = "5")
    private int breakSeconds;
    
    @Option(names = {"--cycles"}, 
            description = "循环次数", 
            defaultValue = "1")
    private int cycles;
    
    private PomodoroTimer pomodoroTimer;
    
    public TimerCommand(PomodoroTimer pomodoroTimer) {
        this.pomodoroTimer = pomodoroTimer;
    }
    
    @Override
    public void run() {
        try {
            if (workSeconds <= 0 || breakSeconds <= 0 || cycles <= 0) {
                System.err.println("错误: 工作时间、休息时间和循环次数都必须大于0");
                return;
            }
            
            if (pomodoroTimer.isRunning()) {
                System.err.println("错误: Pomodoro计时器已在运行中");
                return;
            }
            
            pomodoroTimer.startTimer(workSeconds, breakSeconds, cycles);
            
        } catch (Exception e) {
            System.err.println("意外错误: " + e.getMessage());
        }
    }
}
