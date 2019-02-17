import { AxiosError } from 'axios';

/**
 * Extracts an error message from an [AxiosError].
 * 
 * @param axiosObject the axios error
 * @return the error message
 */
export function extractErrorMessage(axiosObject: AxiosError): string {
  if (axiosObject.response) {
    if(axiosObject.response.status == 502) {
      return "The Server's down but the frontend is still going strong... Try again in about 20 seconds please."
    }
    return axiosObject.response.data['error'] || 'Got an unknown error (' + axiosObject.response.status + ')'
  } else if (axiosObject.request) {
    return 'No connection could be established.'
  } else {
    return 'Error creating your request.'
  }
}

/**
 * Checks whether a jwt is still valid (i.e. exists and is not expired).
 * 
 * @param jwtString the jwt as a string
 */
export function isJwtValid(jwtString: string | null | undefined): boolean {
  if (!jwtString || jwtString.length < 1) {
    return false
  }

  return !isJwtExpired(jwtString)
}

function isJwtExpired(jwt: string): boolean {
  let parts = jwt.split('.')

  if (parts.length !== 3) {
    return false
  }

  const claimsString = atob(parts[1])
  const claims = JSON.parse(claimsString)

  return claims['exp'] && ((new Date()).getTime() / 1000) > claims['exp']
}
