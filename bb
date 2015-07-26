#!/bin/bash

DEV=$1
TARGET=/tmp/bootbuddy
ISO_DIR=/tmp/iso
BB_WIZ=/usr/local/lib/bootbuddy/bb_wiz.rb
FDISK=`which fdisk`
MKFS=`which mkfs.vfat`
MOUNT=`which mount`
UMOUNT=`which umount`
SYNC=`which sync`
EJECT=`which eject`
MKDIR=`which mkdir`
RMDIR=`which rmdir`
DD=`which dd`
RSYNC="`which rsync` -og --size-only"
GRUB_INSTALL="`which grub-install` --force --no-floppy --removable"
GREP=`which grep`
SLEEP=`which sleep`
ECHO=`which echo`

if [ -z ${DEV} ]; then
  ${ECHO} "usage: bb BLOCKDEV"
  exit 1
fi
if [ ! -b ${DEV} ]; then
  ${ECHO} "FAILED: This is not a valid blockdevice!"
  exit 1
fi

${ECHO} "Creating work directories"
${MKDIR} -vp ${ISO_DIR}
${MKDIR} -vp ${TARGET}

${ECHO} -n "Testing presence of existing BootBuddy... "
${FDISK} -l ${DEV} | ${GREP} ^/dev | ${GREP} -q "W95 FAT32" && \
  ( ${GREP} -q ${TARGET} /proc/mounts || ${MOUNT} LABEL=BOOTBUDDY ${TARGET} 2>/dev/null )

if [ -e ${TARGET}/.bootbuddy ]; then
  ${ECHO} "BootBuddy drive identified! :)"
  ${ECHO} "(Re)creating base directory structure"
  ${MKDIR} -vp ${TARGET}/{iso,boot}
  ${MKDIR} -vp ${TARGET}/boot/grub
  ${MKDIR} -vp ${TARGET}/EFI/BOOT
else
  ${ECHO} "BootBuddy not found :("
  ${UMOUNT} -f ${DEV}[0-9] 2> /dev/null
  ${ECHO} "Wiping MBR on ${DEV}"
  ${DD} if=/dev/zero of=${DEV} bs=512 count=1 2> /dev/null

  ${ECHO} "Creating new MBR on ${DEV}"
  ${ECHO} -e 'n\np\n1\n\n\n\nt\n0b\n\nw\nq\n' | ${FDISK} ${DEV} >/dev/null 2>&1

  ${ECHO} -n 'Replug the device... '
  while [ -b ${DEV} ]; do ${SLEEP} 0.2; done
  ${ECHO} 'unplugged'

  ${ECHO} -n "Waiting for the ${DEV} to appear..."
  while [ ! -b ${DEV} ]; do ${ECHO} -n .; sleep 0.2; done
  ${ECHO} " :)"
  ${SLEEP} 2

  ${ECHO} "Creating filesystem"
  ${MKFS} -n BOOTBUDDY ${DEV}1
  ${ECHO} "Mounting into ${TARGET}"
  ${MOUNT} ${DEV}1 ${TARGET} || exit 1

  ${ECHO} "Creating base directory structure"
  ${MKDIR} -vp ${TARGET}/{iso,boot}
  ${MKDIR} -vp ${TARGET}/boot/grub
  ${MKDIR} -vp ${TARGET}/EFI/BOOT

fi

${ECHO} -n > ${TARGET}/.bootbuddy

${ECHO} "Copying UEFI boot files"
${RSYNC} --info=FLIST2,NAME2 /usr/local/lib/bootbuddy/uefi/* ${TARGET}/EFI/BOOT/

${ECHO} "Downloading ISO images into ${ISO_DIR}"
${BB_WIZ} -d ${ISO_DIR}

${ECHO} "Copying ISOs to flash drive"
${RSYNC} --info=FLIST2,PROGRESS1,NAME2,REMOVE --human-readable --delete-excluded --include-from=<(${BB_WIZ} -s) --exclude=* -r ${ISO_DIR}/ ${TARGET}/iso/

${ECHO} "Syncing ${DEV}"
${SYNC} && ${SYNC}
${ECHO} "Generating ${TARGET}/boot/grub/grub.cfg"
${BB_WIZ} -g > ${TARGET}/boot/grub/grub.cfg

${ECHO} "Installing GRUB (BIOS bootloader) onto ${DEV}"
${GRUB_INSTALL} --boot-directory=${TARGET}/boot ${DEV} || exit 1

${ECHO} "Installing GRUB (UEFI bootloader) onto ${DEV}"
${GRUB_INSTALL} --boot-directory=${TARGET}/boot --efi-directory=${TARGET}/EFI/BOOT ${DEV} || exit 1

${ECHO} "Syncing ${DEV}"
${SYNC} && ${SYNC}
${ECHO} "Unmounting"
${UMOUNT} ${TARGET}
${ECHO} "Removing work directory ${TARGET}"
${RMDIR} ${TARGET}
${EJECT} ${DEV}

${ECHO} -e "\e[0;32m"
${ECHO} -e "\t+---------------------------------------+"
${ECHO} -e "\t|   The BootBuddy USB stick is ready!   |"
${ECHO} -e "\t|    Feel free to unplug and use it!    |"
${ECHO} -e "\t+---------------------------------------+"
${ECHO} -e "\e[0m"
