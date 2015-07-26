# BootBuddy

## About
This project aims to simplify and automate the process of creating custom multiboot USB flash drives. Highly usable by SysAdmin/DevOps guys.

 * Creates both BIOS and UEFI boot capable flash disk
 * Manages GRUB config file and synces needed ISOs
 * Holds cache with downloaded ISOs on computer (/tmp/iso)
 * Update-capable - no need to reformat the whole drive on changes

## Prerequisities

 * USB flash drive (of any size, depends on how much stuff you want to fit in)
 * Ruby (1.8.7 or newer)
  * when on 1.8.7, make sure you run ```gem install yaml --no-rdoc --no-ri``` (or something equivalent)
  * TIP: you can test whether you already have the YAML lib by running ```ruby -e 'if require "yaml" then puts "OK" else puts "ERR" end'```
 * GRUB2
 * root access (like ```sudo bash``` or something)

## Installation

 1. ```git clone https://github.com/blufor/bootbuddy.git```
 2. ```cd bootbuddy```
 3. ```sudo ./INSTALL.sh```

## Usage

 1. Configure your distros in ```/etc/bootbuddy.yaml```
 2. Plugin you USB drive. Be prepared to lose all the data on it (so backup if you feel the need).
 3. Find out the device name by ```fdisk -l``` or ```lsblk```
 4. Run ```bb /dev/sdX``` and follow the instructions.

## Tips

 * If you have problems, investigate the code, it's really simple
 * Eventually, you can post a pull request with a fix or enhancement ;)
 * Don't hesitate also to post pull requests with more templates. I've only added systems egoistically based on my needs ;)
