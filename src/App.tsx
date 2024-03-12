/**
 * Sample React Native App
 * https://github.com/facebook/react-native
 *
 * @format
 */

import React, {
  useEffect,
  useState,
} from 'react';
import type { PropsWithChildren } from 'react';
import {
  StatusBar,
  StyleSheet,
  useColorScheme,
  View,
  TextInput,
  Dimensions,
  Button,
  Pressable,
  Platform,
  Text as RNText
} from 'react-native';

import {
  Canvas,
  Circle,
  DiscretePathEffect,
  Group,
  Line,
  LinearGradient,
  RoundedRect,
  Text,
  useFont,
  vec,
  matchFont,
  Skia,
  Path,
  SkRect,
  SweepGradient,
  rotate,
  Transforms3d
} from '@shopify/react-native-skia';

import Alarm, { cancelAlarm, scheduleAlarm, stopAlarm } from './alarm';

const WIDTH = Dimensions.get('window').width
const HEIGHT = WIDTH

const R = 0.9 * WIDTH / 2

// Copied from Dont.d.ts
type Slant = "normal" | "italic" | "oblique";
type Weight = "normal" | "bold" | "100" | "200" | "300" | "400" | "500" | "600" | "700" | "800" | "900";

const fontFamily = Platform.select({ ios: "Helvetica", android: "Roboto", default: "serif" });
const fontStyle = {
  fontFamily,
  fontSize: 30,
  fontStyle: "normal" as Slant,
  fontWeight: "bold" as Weight,
};
const font = matchFont(fontStyle);

const clockFontStyle = {
  fontFamily: "serif",
  fontSize: 30,
  fontStyle: "normal" as Slant,
  fontWeight: "bold" as Weight,
}
const clockFont = matchFont(clockFontStyle)

const HourClockFace = () => {
  return (
    <Group>
      <Circle cx={HEIGHT / 2} cy={WIDTH / 2} r={R} color="#1B1B1B" />
      <Group origin={{ x: R, y: R }} transform={[{ scale: 0.85 }]}>
        {new Array(24).fill(0).map((_, index) => {
          // deviation to adjust the difference after the calculation
          const dx = R * 0.06;
          const dy = R * 0.18;

          const angle = Math.PI / -2 + (2 * index * Math.PI) / 12;

          let x = R * Math.cos(angle) + (R + dx);
          let y = R * Math.sin(angle) + (R + dy);

          if (index > 11) {
            x = 0.7 * R * Math.cos(angle) + (R + dx);
            y = 0.7 * R * Math.sin(angle) + (R + dy);
          }


          return (
            <Text
              font={clockFont}
              y={y}
              x={x}
              text={`${index === 0 ? '00' : index}`}
              color="#AAAAAA"
              key={index}
            />
          );
        })}
      </Group>
    </Group>
  )
}

const MinuteClockFace = () => {
  return (
    <Group>
      <Circle cx={HEIGHT / 2} cy={WIDTH / 2} r={R} color="#1B1B1B" />
      <Group origin={{ x: R, y: R }} transform={[{ scale: 0.85 }]}>
        {new Array(12).fill(0).map((_, index) => {
          // deviation to adjust the difference after the calculation
          const dx = R * 0.06;
          const dy = R * 0.18;

          const angle = Math.PI / -2 + (2 * index * Math.PI) / 12;

          let x = R * Math.cos(angle) + (R + dx);
          let y = R * Math.sin(angle) + (R + dy);

          return (
            <Text
              font={clockFont}
              y={y}
              x={x}
              text={`${index === 0 ? '00' : index * 5}`}
              color="#AAAAAA"
              key={index}
            />
          );
        })}
      </Group>
    </Group>
  )
}

const DurationClockFace = () => {
  return (
    <Group>
      <Circle cx={HEIGHT / 2} cy={WIDTH / 2} r={R} color="#1B1B1B" />
      <Group origin={{ x: R, y: R }} transform={[{ scale: 0.85 }]}>
        {new Array(12).fill(0).map((_, index) => {
          // deviation to adjust the difference after the calculation
          const dx = R * 0.06;
          const dy = R * 0.18;

          const angle = Math.PI / -2 + (2 * index * Math.PI) / 12;

          let x = R * Math.cos(angle) + (R + dx);
          let y = R * Math.sin(angle) + (R + dy);

          return (
            <Text
              font={clockFont}
              y={y}
              x={x}
              text={`${index === 0 ? '12' : index}`}
              color="#AAAAAA"
              key={index}
            />
          );
        })}
      </Group>
    </Group>
  )
}

type State = 'hour' | 'minute' | 'duration' | 'set'


const getHourAngle = (hours: number, minutes: number) => {
  return ((hours % 12) / 12) * 2 * Math.PI + (minutes / 60) * (1 / 12) * 2 * Math.PI
}
const getMinuteAngle = (minutes: number) => {
  return (minutes / 60) * 2 * Math.PI
}

const coordsToAngleDeg = (coords: [number, number]): number => {
  if (!coords) return 0
  const ox = WIDTH / 2
  const oy = HEIGHT / 2
  const cx = coords[0]
  const cy = coords[1]
  const val = Math.atan((ox - cx) / (oy - cy))
  let angle: number = 360
  //console.log('val:', val)
  if (cx > ox && cy < oy) { // top-right quadrant
    angle = -val * 360 / (2 * Math.PI)
  }
  if (cx > ox && cy > oy) { // bottom-right quadrant
    angle = (-val + Math.PI) * 360 / (2 * Math.PI)
  }
  if (cx < ox && cy > oy) { // bottom-left quadrant
    angle = (-val + Math.PI) * 360 / (2 * Math.PI)
  }
  if (cx < ox && cy < oy) { // top-left quadrant
    angle = (-val + 2 * Math.PI) * 360 / (2 * Math.PI)
  }
  return angle
}

interface ClockPointerProps {
  coords: [number, number] | null
  hours: number
  minutes: number
  origin: [number, number]
  r: number
  selectionState: State
}

const ClockPointers = ({ coords, hours, minutes, origin, r, selectionState }: ClockPointerProps) => {
  if (!coords) return
  const vecLen = Math.sqrt(((origin[0] - coords[0]) * (origin[0] - coords[0])) + ((origin[1] - coords[1]) * (origin[1] - coords[1])))
  const unitVec: [number, number] = [(coords[0] - origin[0]) / vecLen, (coords[1] - origin[1]) / vecLen]

  const hourPath = Skia.Path.Make()
  const minutePath = Skia.Path.Make()
  const durationPath = Skia.Path.Make()
  hourPath.moveTo(origin[0], origin[1])
  minutePath.moveTo(origin[0], origin[1])
  durationPath.moveTo(origin[0], origin[1])
  let circleHighLight = [...origin]
  switch (selectionState) {
    case 'set':
    case 'duration': {
      // hours set, use it for angle
      const hourLen = 0.6 * R
      const hangle = getHourAngle(hours, minutes)
      const hx = origin[0] + hourLen * Math.sin(hangle)
      const hy = origin[1] - hourLen * Math.cos(hangle)
      hourPath.lineTo(hx, hy)
      hourPath.close()

      const minLen = 0.8 * R
      const mangle = getMinuteAngle(minutes)
      const mx = origin[0] + minLen * Math.sin(mangle)
      const my = origin[1] - minLen * Math.cos(mangle)
      minutePath.lineTo(mx, my)
      minutePath.close()

      const scaledCoords = [origin[0] + R * 0.6 * unitVec[0], origin[1] + R * 0.6 * unitVec[1]]

      const highlightCoords = [origin[0] + R * 0.8 * unitVec[0], origin[1] + R * 0.8 * unitVec[1]]

      const ellipse: SkRect = {
        x: origin[0] / 4 + R * 0.03,
        y: origin[1] / 4 + R * 0.03,
        width: 1.6 * R,
        height: 1.6 * R
      }
      durationPath.lineTo(scaledCoords[0], scaledCoords[1])
      const mDegrees = (mangle / (2 * Math.PI)) * 360
      let dDegrees = coordsToAngleDeg([highlightCoords[0], highlightCoords[1]])
      if (dDegrees > mDegrees) dDegrees -= 360
      durationPath.addArc(ellipse, mDegrees - 90, (dDegrees - mDegrees))
      durationPath.lineTo(origin[0], origin[1])
      durationPath.close()

      circleHighLight = [highlightCoords[0], highlightCoords[1]]
      break
    }
    case 'hour': {
      const scaledR = vecLen < 0.7 * r ? 0.6 * R : R
      const scaledCoords = [origin[0] + scaledR * 0.8 * unitVec[0], origin[1] + scaledR * 0.8 * unitVec[1]]
      hourPath.lineTo(scaledCoords[0], scaledCoords[1])
      hourPath.close()
      circleHighLight = [...scaledCoords]
      break
    }
    case 'minute': {
      const scaledCoords = [origin[0] + R * 0.8 * unitVec[0], origin[1] + R * 0.8 * unitVec[1]]
      minutePath.lineTo(scaledCoords[0], scaledCoords[1])
      minutePath.close()
      circleHighLight = [...scaledCoords]
      break
    }
  }

  const showHour = selectionState === 'hour' || selectionState === 'duration' || selectionState === 'set'
  const showMinute = selectionState === 'minute' || selectionState === 'duration' || selectionState === 'set'
  const showDuration = selectionState === 'duration' || selectionState === 'set'

  return (
    <Group>
      {showHour && <Path path={hourPath} color={selectionState === 'hour' ? "#44448888" : "#BBBBBB66"} style="stroke" strokeWidth={3} />}
      {showMinute && <Path path={minutePath} color={selectionState === 'minute' ? "#444488" : "#BBBBBB66"} style="stroke" strokeWidth={3} />}
      {showDuration && <Path path={durationPath} color={selectionState === 'set' ? '#BBBBBB66' : "#6666BB66"}><SweepGradient mode='repeat' c={vec(origin[0], origin[1])} start={-90 + 6 * minutes} end={270 + 6 * minutes} colors={selectionState === 'set' ? ['#222222', '#BBBBBB66'] : ['#222222', "#4444AA"]} /></Path>}
      <Circle cx={circleHighLight[0]} cy={circleHighLight[1]} r={r * 0.1} color={selectionState === 'set' ? '#BBBBBB66' : "#444488FF"} blendMode='src' />

    </Group>
  )
}

const getStarterCoords = (): [number, number] => {
  const hours = (new Date().getHours() + 1) % 24
  const r = hours < 12 ? R : 0.3 * R
  const x = WIDTH / 2 + r * Math.sin(2 * Math.PI * hours / 12)
  const y = HEIGHT / 2 - r * Math.cos(2 * Math.PI * hours / 12)
  return [x, y]
}

const App = (): React.JSX.Element => {
  const [alarmSet, setAlarmSet] = React.useState<boolean>(true)
  const [coords, setCoords] = React.useState<[number, number]>(getStarterCoords())
  const [hours, setHours] = React.useState<number>((new Date().getHours() + 1) % 24)
  const [minutes, setMinutes] = React.useState<number>(0)
  const [duration, setDuration] = React.useState<number>(20)
  const [selectionState, setSelectionState] = React.useState<State>('hour')
  const isDarkMode = useColorScheme() === 'dark';


  const getClockFace = () => {
    switch (selectionState) {
      case 'hour': {
        return <HourClockFace />
      }
      case 'minute': {
        return <MinuteClockFace />
      }
      case 'set':
      case 'duration': {
        return <DurationClockFace />
      }
    }
  }

  const getHour = (): string => {
    if (!coords) return "00"
    const ox = WIDTH / 2
    const oy = HEIGHT / 2
    const cx = coords[0]
    const cy = coords[1]
    const vecLen = Math.sqrt((ox - cx) * (ox - cx) + (oy - cy) * (oy - cy))
    const val = Math.atan((ox - cx) / (oy - cy))
    let hour: number = 12
    //console.log('val:', val)
    if (cx > ox && cy < oy) { // top-right quadrant
      hour = -val * 12 / (2 * Math.PI)
    }
    if (cx > ox && cy > oy) { // bottom-right quadrant
      hour = (-val + Math.PI) * 12 / (2 * Math.PI)
    }
    if (cx < ox && cy > oy) { // bottom-left quadrant
      hour = (-val + Math.PI) * 12 / (2 * Math.PI)
    }
    if (cx < ox && cy < oy) { // top-left quadrant
      hour = (-val + 2 * Math.PI) * 12 / (2 * Math.PI)
    }
    hour = Math.round(hour)
    if (vecLen < 0.7 * R && hour < 12) hour = hour + 12
    return hour.toString().padStart(2, '0')
  }

  const getMinutes = (): string => {
    if (!coords) return '00'
    const ox = WIDTH / 2
    const oy = HEIGHT / 2
    const cx = coords[0]
    const cy = coords[1]
    const vecLen = Math.sqrt((ox - cx) * (ox - cx) + (oy - cy) * (oy - cy))
    const val = Math.atan((ox - cx) / (oy - cy))
    let minutes: number = 60
    //console.log('val:', val)
    if (cx > ox && cy < oy) { // top-right quadrant
      minutes = -val * 60 / (2 * Math.PI)
    }
    if (cx > ox && cy > oy) { // bottom-right quadrant
      minutes = (-val + Math.PI) * 60 / (2 * Math.PI)
    }
    if (cx < ox && cy > oy) { // bottom-left quadrant
      minutes = (-val + Math.PI) * 60 / (2 * Math.PI)
    }
    if (cx < ox && cy < oy) { // top-left quadrant
      minutes = (-val + 2 * Math.PI) * 60 / (2 * Math.PI)
    }
    minutes = Math.round(minutes - 0.5)
    return minutes.toString().padStart(2, '0')
  }

  //console.log(getHour())

  const handleMove = (event: any) => {
    if (selectionState === 'set') return
    const coords: [number, number] = [event.nativeEvent.locationX, event.nativeEvent.locationY]
    //console.log('bb', coords)
    setCoords(coords)

    if (selectionState === 'duration') {
      const dAngle = coordsToAngleDeg([coords[0], coords[1]])
      const mAngle = minutes * 6
      let angleDiff = mAngle - dAngle
      if (dAngle > mAngle) angleDiff += 360
      const diff = Math.round(angleDiff / 6 - 0.5)
      setDuration(diff)
    }
  }

  const handleStop = (event: any) => {
    switch (selectionState) {
      case 'hour': {
        setHours(Number(getHour()))
        setSelectionState('minute')
        break
      }
      case 'minute': {
        setMinutes(Number(getMinutes()))
        setSelectionState('duration')
        // set duration to 15min and coords to that
        if (!coords) break
        const angle = coordsToAngleDeg([coords[0], coords[1]]) - 120 // 120deg = 20min default
        const newX = WIDTH / 2 + Math.sin(2 * Math.PI * angle / 360)
        const newY = HEIGHT / 2 - Math.cos(2 * Math.PI * angle / 360)
        setCoords([newX, newY])
        setDuration(20)
        break
      }
    }

  }

  const handleSetAlarm = () => {
    if (selectionState == 'duration') {
      setSelectionState('set')
      const alarmData = new Alarm({ hour: hours, minutes: minutes, enabled: true })
      scheduleAlarm({ hour: hours, minutes: minutes, enabled: true, duration: duration })
    } else if (selectionState === 'set') {
      cancelAlarm()
      setSelectionState('duration')
    }

  }

  const handleStopAlarm = () => {
    if (selectionState === 'set')
      stopAlarm()
  }

  const handleActivateHours = () => {
    // get coords for current hours
    // set coords to that value
    setSelectionState('hour')
  }

  const handleActivateMinutes = () => {
    // get coords for current minutes
    // set coords to that value
    setSelectionState('minute')
  }

  const handleActivateDuration = () => {
    setSelectionState('duration')
  }


  return (
    <View style={{ padding: 0, flex: 1, flexDirection: 'column', alignItems: 'center', backgroundColor: '#111111' }}>
      <RNText style={{ padding: 20, fontSize: 26, alignSelf: 'center', color: "#AAAAAA" }}>{selectionState === 'set' ? '' : `Aseta ${selectionState === 'duration' ? 'aikaisin' : 'viimeisin'} herätysaika`}</RNText>
      <View style={{ flexDirection: 'row', padding: 10, borderRadius: 10 }}>
        <RNText
          onPress={handleActivateHours}
          style={{
            padding: 10,
            borderRadius: 10,
            fontWeight: '800',
            fontSize: 40,
            backgroundColor: selectionState === 'hour' ? '#4444AA' : "#333333",
            color: "#AAAAAA"
          }}>
          {selectionState === 'hour' ? getHour() : hours.toString().padStart(2, '0')}
        </RNText>
        <RNText
          style={{
            paddingVertical: 10,
            paddingHorizontal: 2,
            fontWeight: '800',
            fontSize: 40,
            color: "#AAAAAA"
          }}>
          :
        </RNText>
        <RNText
          onPress={handleActivateMinutes}
          style={{
            padding: 10,
            borderRadius: 10,
            fontWeight: '800',
            fontSize: 40,
            backgroundColor: selectionState === 'minute' ? '#4444AA' : "#333333",
            color: "#AAAAAA"
          }}>
          {selectionState === 'minute' ? getMinutes() : minutes.toString().padStart(2, '0')}
        </RNText>
      </View>
      {/* <View style={{ flexDirection: 'row', borderRadius: 10, backgroundColor: selectionState === 'duration' ? '#444488' : "#333333" }} >

        <RNText
          style={{
            padding: 10,
            fontWeight: '800',
            fontSize: 40,
            color: "#AAAAAA"
          }}>
          {!hours ? '00' : (hours + Math.round((minutes - duration) / 60 - 0.5)).toString().padStart(2, '0')}
        </RNText>
        <RNText
          style={{
            paddingVertical: 10,
            paddingHorizontal: 2,
            fontWeight: '800',
            fontSize: 40,
            color: "#AAAAAA"
          }}>:</RNText>
        <RNText
          style={{
            padding: 10,
            fontWeight: '800',
            fontSize: 40,
            color: "#AAAAAA"
          }}>
          {!minutes ? '00' : ((minutes - duration + 60) % 60).toString().padStart(2, '0')}
        </RNText>
      </View> */}
      <Pressable
        onTouchMove={handleMove}
        onTouchEnd={handleStop}
      >
        <Canvas style={{ width: WIDTH, height: HEIGHT, marginTop: 0 }}>
          <Group>
            {getClockFace()}
            <ClockPointers coords={coords} origin={[HEIGHT / 2, WIDTH / 2]} r={R} hours={hours} minutes={minutes} selectionState={selectionState} />
          </Group>
        </Canvas>
      </Pressable>
      <View style={{ flexDirection: 'row', borderRadius: 10, backgroundColor: selectionState === 'duration' ? '#444488' : "#333333" }}>
        <RNText
          onPress={handleActivateDuration}
          style={{
            padding: 10,
            fontWeight: '800',
            fontSize: 40,
            color: "#AAAAAA"
          }}>
          {`${!hours ? '00' : (hours + Math.round((minutes - duration) / 60 - 0.5)).toString().padStart(2, '0')} : ${!minutes ? '00' : ((minutes - duration + 60) % 60).toString().padStart(2, '0')} - ${selectionState === 'hour' ? getHour() : hours.toString().padStart(2, '0')} : ${selectionState === 'minute' ? getMinutes() : minutes.toString().padStart(2, '0')}`}
        </RNText>
      </View>
      <View style={{ flexDirection: 'row', justifyContent: 'space-between', margin: 20 }}>
        <Button
          onPress={handleSetAlarm}
          title={selectionState === 'set' ? 'Poista herätys' : 'Aseta herätys'}
          color={(selectionState === 'duration' || selectionState === 'set') ? '#4444AA' : '#333333'}
        />
        {/*<Button
          onPress={handleStopAlarm}
          disabled={selectionState !== 'set'}
          title="stop alarm"
          color="#4444AA"
        />*/}
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  sectionContainer: {
    marginTop: 32,
    paddingHorizontal: 24,
  },
  sectionTitle: {
    fontSize: 24,
    fontWeight: '600',
  },
  sectionDescription: {
    marginTop: 8,
    fontSize: 18,
    fontWeight: '400',
  },
  highlight: {
    fontWeight: '700',
  },
});

export default App;
