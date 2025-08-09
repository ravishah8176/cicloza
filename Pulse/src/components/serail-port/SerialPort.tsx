import { useEffect, useState } from 'react'
import styles from './serailport.module.scss'
import { SerailPortService } from '../../services/serailPort.service'

export const SerialPort = () => {
  // State for port configuration
  const [availablePorts, setAvailablePorts] = useState<string[]>([])
  const [selectedPort, setSelectedPort] = useState<string>('')
  const [selectedBaudRate, setSelectedBaudRate] = useState<string>('')
  const [selectedDataBits, setSelectedDataBits] = useState<string>('')
  const [selectedStopBits, setSelectedStopBits] = useState<string>('')
  const [incomingData, setIncomingData] = useState<string>('')
  const [isConnected, setIsConnected] = useState<boolean>(false)
  const selectedParameters = {
    port: selectedPort,
    baudRate: selectedBaudRate,
    dataBits: selectedDataBits,
    stopBits: selectedStopBits,
    parity: 'NONE'
  }
  const { getAvailablePorts } = SerailPortService()
  
  // State for available ports
  const [loading, setLoading] = useState<boolean>(true)
  const [error, setError] = useState<string>('')
  const fetchPorts = async () => {
    try {
      setLoading(true)
      const ports = await getAvailablePorts()
      setAvailablePorts(ports)
      setError('')
    } catch (err) {
      setError('Failed to load available ports. Please check if the server is running.')
      console.error('Error fetching ports:', err)
    } finally {
      setLoading(false)
    }
  }

  // Create port options array with placeholder
  const portOptions = [
    { value: '', label: 'Select a port', disabled: true },
    ...availablePorts.map(port => ({ value: port, label: port, disabled: false }))
  ]

  const selectPort = (e: React.ChangeEvent<HTMLSelectElement>) => {
    setSelectedPort(e.target.value)
    console.log(selectedParameters)
    console.log(`The selected port is ${e.target.value}`)
  }

  // Create baud rate options array
  const baudRateOptions = [
    { value: '', label: 'Select a baud rate', disabled: true },
    { value: '4800', label: '4800' },
    { value: '9600', label: '9600' },
    { value: '19200', label: '19200' },
    { value: '38400', label: '38400' },
    { value: '57600', label: '57600' },
    { value: '115200', label: '115200' }
  ]

  const selectBaudRate = (e: React.ChangeEvent<HTMLSelectElement>) => {
    setSelectedBaudRate(e.target.value)
    console.log(`The selected baud rate is ${e.target.value}`)
  }

  // Create data bits options array
  const dataBitsOptions = [
    { value: '', label: 'Select a data bits', disabled: true },
    { value: '5', label: '5' },
    { value: '6', label: '6' },
    { value: '7', label: '7' },
  ]

  const selectDataBits = (e: React.ChangeEvent<HTMLSelectElement>) => {
    setSelectedDataBits(e.target.value)
    console.log(`The selected data bits is ${e.target.value}`)
  }

  // Create stop bits options array
  const stopBitsOptions = [
    { value: '', label: 'Select a stop bits', disabled: true },
    { value: '1', label: '1' },
    { value: '1.5', label: '1.5' },
    { value: '2', label: '2' },
  ]

  const selectStopBits = (e: React.ChangeEvent<HTMLSelectElement>) => {
    setSelectedStopBits(e.target.value)
    console.log(`The selected stop bits is ${e.target.value}`)
  }

  return (
    <div className={styles.container}>
      <div className={styles.formGrid}>
        <div className={styles.formGroup}>
          <label htmlFor="port-select" className={styles.label}>Available ports</label>
          <select
            id="port-select"
            value={selectedPort}
            className={styles.select}
            aria-label="Select a port"
            onChange={selectPort}
            onClick={fetchPorts}
          >
            {portOptions.map((option, index) => (
              <option 
                key={index} 
                value={option.value} 
                disabled={option.disabled}
              >
                {option.label}
              </option>
            ))}
          </select>
        </div>

        <div className={styles.formGroup}>
          <label htmlFor="baud-select" className={styles.label}>Baud rate</label>
          <select
            id="baud-select"
            value={selectedBaudRate}
            onChange={selectBaudRate}
            className={styles.select}
            aria-label="Select baud rate"
          >
            {baudRateOptions.map((option, index) => (
              <option 
                key={index} 
                value={option.value} 
                disabled={option.disabled}
              >
                {option.label}
              </option>
            ))}
          </select>
        </div>

        <div className={styles.formGroup}>
          <label htmlFor="data-bits-select" className={styles.label}>Data bits</label>
          <select
            id="data-bits-select"
            value={selectedDataBits}
            onChange={selectDataBits}
            className={styles.select}
            aria-label="Select data bits"
          >
            {dataBitsOptions.map((option, index) => (
              <option 
                key={index} 
                value={option.value} 
                disabled={option.disabled}
              >
                {option.label}
              </option>
            ))}
          </select>
        </div>

        <div className={styles.formGroup}>
          <label htmlFor="stop-bits-select" className={styles.label}>Stop bits</label>
          <select
            id="stop-bits-select"
            value={selectedStopBits}
            onChange={selectStopBits}
            className={styles.select}
            aria-label="Select stop bits"
          >
            {stopBitsOptions.map((option, index) => (
              <option 
                key={index} 
                value={option.value} 
                disabled={option.disabled}
              >
                {option.label}
              </option>
            ))}
          </select>
        </div>
      </div>

      <div className={styles.buttonGroup}>
        <button
          disabled={isConnected}
          className={styles.connectButton}
        >
          Connect
        </button>
        <button 
          disabled={!isConnected}
          className={styles.disconnectButton}
        >
          Disconnect
        </button>
      </div>

      {error && <div className={styles.errorMessage}>{error}</div>}

      <div className={styles.incomingDataContainer}>
        <label htmlFor="incoming-data" className={styles.label}>Incoming data</label>
        <textarea
          id="incoming-data"
          value={incomingData}
          readOnly
          className={styles.textarea}
        />
      </div>
    </div>
  )
}