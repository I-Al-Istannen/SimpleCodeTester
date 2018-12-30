import { ActionTree } from 'vuex';
import axios, { AxiosPromise } from 'axios';
import { CheckResultState, CheckResult, FileCheckResult, UserLoginInfo, Pair } from '../../types';
import { RootState } from '../../types';

function parseCheckResponse(json: any) {
  const entries = new Array<Pair<string, Array<FileCheckResult>>>()
  Object.keys(json.fileResults).forEach(fileName => {
    const checkResults = json.fileResults[fileName] as Array<FileCheckResult>;
    entries.push(new Pair(fileName, checkResults));
  })

  return new CheckResult(entries)
}

export const actions: ActionTree<CheckResultState, RootState> = {
  checkSingle({ commit, state }, payload: string): AxiosPromise<any> {
    return axios.post("/test/single", payload, {
      headers: { "Content-Type": "text/plain" }
    }).then(response => {
      commit("checkResult", parseCheckResponse(response.data))

      return response
    });
  },
  checkMultiple({ commit, state }, payload: Array<File>): AxiosPromise<any> {
    const formData = new FormData()
    payload.forEach(file => {
      formData.append(file.name, file)
    })
    return axios.post("/test/multiple", formData)
      .then(response => {
        commit("checkResult", parseCheckResponse(response.data))

        return response
      })
  },
  checkZip({ commit, state }, payload: File): AxiosPromise<any> {
    const formData = new FormData()
    formData.append("file", payload)
    return axios.post("/test/zip", formData)
      .then(response => {
        commit("checkResult", parseCheckResponse(response.data))

        return response
      })
  }

};