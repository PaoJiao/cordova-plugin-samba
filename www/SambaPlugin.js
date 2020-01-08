const exec = require('cordova/exec')
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

  upload(localPath, smbPath, success, error) {
    exec(success, error, 'SambaPlugin', 'upload', [ localPath, smbPath ])
  },

  mkfile(path, success, error) {
    exec(success, error, 'SambaPlugin', 'mkfile', [ path ])
  },

  mkdir(path, success, error) {
    exec(success, error, 'SambaPlugin', 'mkdir', [ path ])
  },

  delete(path, success, error) {
    exec(success, error, 'SambaPlugin', 'delete', [ path ])
  },

  wol(mac, success, error) {
    exec(success, error, 'SambaPlugin', 'wol', [ mac ])
  }

}
