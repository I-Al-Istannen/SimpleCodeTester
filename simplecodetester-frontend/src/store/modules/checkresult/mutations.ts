import { MutationTree } from 'vuex';
import { CheckResultState, CheckResult } from '../../types';

export const mutations: MutationTree<CheckResultState> = {
  checkResult(state: CheckResultState, payload: CheckResult) {
    state.checkResult = payload;
  }
};