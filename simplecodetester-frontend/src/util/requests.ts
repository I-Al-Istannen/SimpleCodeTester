import { AxiosError } from 'axios';

/**
 * Extracts an error message from an [AxiosError].
 * 
 * @param axiosObject the axios error
 * @return the error message
 */
export function extractErrorMessage(axiosObject: AxiosError): string {
  if (axiosObject.response) {
    return axiosObject.response.data['error'] || 'Got an unknown error (' + axiosObject.response.status + ')'
  } else if (axiosObject.request) {
    return 'No connection could be established.'
  } else {
    return 'Error creating your request.'
  }
}
