import { NativeModules } from "react-native";
import 'react-native-get-random-values'
import { v4 as uuid } from 'uuid'


//const AlarmService = NativeModules.AlarmModule
const { AlarmModule } = NativeModules
//console.log('nm:', NativeModules, AlarmModule)

export const bleDebug = async () => {
    await AlarmModule.bleDebug();
}

export const bleStop = async () => {
    await AlarmModule.bleStop();
}

export const scheduleAlarm = async (alarm: any) => {
    if (!(alarm instanceof Alarm)) {
        alarm = new Alarm(alarm)
    }
    await AlarmModule.set(alarm)
}

/* export const enableAlarm = async (uid: string) => {
    await AlarmService.enable(uid)
}

export const disableAlarm = async (uid: string) => {
    await AlarmService.disable(uid)
} */

export const stopAlarm = async () => {
    await AlarmModule.stop()
}

export const cancelAlarm = async () => {
    await AlarmModule.cancel()
}

/*

export const snoozeAlarm = async () => {
    await AlarmService.snooze()
}

export const removeAlarm = async (uid: string) => {
    await AlarmService.remove(uid)
}

export const updateAlarm = async (alarm: any) => {
    if (!(alarm instanceof Alarm)) {
        alarm = new Alarm(alarm)
    }
    await AlarmService.update(alarm)
}

export const removeAllAlarms = async () => {
    await AlarmService.removeAll()
} */

/* export const getAllAlarms = async () => {
    const alarms = await AlarmService.getAll()
    return alarms
}

export const getAlarm = async (uid: string) => {
    const alarm = await AlarmService.get(uid)
    return alarm
}

export const getAlarmState = async () => {
    return AlarmService.getState()
} */

interface AlarmProps {
    uid: string,
    enabled: boolean
    title: string,
    description: string,
    hour: number,
    minutes: number,
    duration: number,
    active: boolean,
}

export default class Alarm {
    uid: string
    enabled: boolean
    title: string
    description: string
    hour: number
    minutes: number
    duration: number
    active: boolean

    constructor(params: Partial<AlarmProps> | null = null) {
        this.uid = getParam(params, 'uid', uuid())
        this.enabled = getParam(params, 'uid', true)
        this.title = getParam(params, 'title', 'Herätys')
        this.description = getParam(params, 'description', 'UniBalance -herätys')
        this.hour = getParam(params, 'hour', new Date().getHours())
        this.minutes = getParam(params, 'minutes', (new Date().getMinutes() + 1) % 60)
        this.duration = getParam(params, "duration", 20)
        this.active = getParam(params, 'active', true)
    }

    static getEmpty = () => {
        return new Alarm({
            title: '',
            hour: 0,
            minutes: 0,
            duration: 20
        })
    }

    getTimeString() {
        const hour = this.hour.toString().padStart(2, '0')
        const minutes = this.minutes.toString().padStart(2, '0')
        return { hour, minutes }
    }

    getTime() {
        const timeDate = new Date()
        timeDate.setMinutes(this.minutes)
        timeDate.setHours(this.hour)
        return timeDate
    }

}

const getParam = (params: any, key: string, defaultValue: any) => {
    try {
        if (params && (params[key] !== null || params[key] !== undefined)) {
            return params[key]
        } else {
            return defaultValue
        }
    } catch (e) {
        return defaultValue
    }
}