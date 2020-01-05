var exec = require('cordova/exec')
module.exports = {

  auth(username, password, success, error) {
    exec(success, error, 'SambaPlugin', 'auth', [ username, password ])
  },

  list(path, success, error) {
    exec(success, error, 'SambaPlugin', 'list', [ path ])
  },

  read(path, success, error) {
    exec(success, error, 'SambaPlugin', 'read', [ path ])
  },

  delete(path, success, error) {
    exec(success, error, 'SambaPlugin', 'delete', [ path ])
  }

}
