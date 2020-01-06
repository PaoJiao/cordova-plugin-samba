# cordova-plugin-samba

## Installation

```
cordova plugin add https://github.com/seatwork/cordova-plugin-samba
```

## Simple Usage

Sets username and password authentication:
```
samba.auth('username', 'password')
```

Lists files and directories by path. The path must be ends with '/', ex. smb://10.0.0.2/sharefolder/directory/
```
samba.list(path, success, error)
```

Reads content by path and return byte[]:
```
samba.read(path, success, error)
```

Uploads local file to smb server:
```
samba.upload(localPath, smbPath, success, error)
```

Creates empty file:
```
samba.mkfile(path, success, error)
```

Creates empty directory:
```
samba.mkdir(path, success, error)
```

Deletes file or directory:
```
samba.delete(ath, success, error)
```
