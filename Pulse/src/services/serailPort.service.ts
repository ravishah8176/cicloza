/**
 * Service to interact with the serial port API
 */
export const SerailPortService = () => {
    /**
     * Get all available serial ports from the API
     * @returns Promise with a list of available ports
     */
    const getAvailablePorts = async (): Promise<string[]> => {
        try {
            const response = await fetch('http://localhost:8080/api/serial/ports');
            
            if (!response.ok) {
                throw new Error(`Error fetching ports: ${response.status} ${response.statusText}`);
            }
            return await response.json();
        } catch (error) {
            console.error('Failed to fetch available ports:', error);
            throw error;
        }
    }

    return {
        getAvailablePorts
    }
}
