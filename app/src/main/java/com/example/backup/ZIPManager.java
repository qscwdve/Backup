package com.example.backup;

import android.app.Activity;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Arrays;
import java.util.Objects;
import java.util.StringTokenizer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class ZIPManager {
    private static final int BUFFER = 800000;
    private static final int BUFFER_SIZE = 1024 * 2;
    private static final int COMPRESSION_LEVEL = 8;
    private static int ZIPFLAG = 0;

    /**
     * 파일 압축
     * @param _files : 압축할 파일 이름 경로 리스트
     * @param zipFileName : 저장될 경로의 파일 이름
     */

    public void zip(String fileName, String basePath, String[] _files, String zipFileName, ProgressDialog dialog, MainActivity mainActivity) {
        AESService aesService = new AESService();
        // zip 시작 플래그
        ZIPFLAG = 1;

        try {
            BufferedInputStream origin = null;
            FileOutputStream dest = new FileOutputStream(zipFileName);
            ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(
                    dest));
            byte[] data = new byte[BUFFER];

            // 진행사항 표시하기
            int totalNum = _files.length;
            int nowNum = 1;

            for (String file : _files) {
                String absoluteFile = basePath + file;
                // 파일 암호화하기
                aesService.encFile(mainActivity, file);

                FileInputStream fi = new FileInputStream(absoluteFile);
                origin = new BufferedInputStream(fi, BUFFER);

                ZipEntry entry = new ZipEntry(absoluteFile.substring(absoluteFile.lastIndexOf("/") + 1));
                out.putNextEntry(entry);
                int count;

                while ((count = origin.read(data, 0, BUFFER)) != -1) {
                    out.write(data, 0, count);
                }
                origin.close();

                // 사용자가 도중에 취소할 경우
                if(ZIPFLAG == 0){
                    out.close();
                    // ZIP 파일 제거
                    mainActivity.innerDeleteFile(zipFileName);
                    aesService.decFile(mainActivity, file);
                    break;
                }
                aesService.decFile(mainActivity, file);

                // 진행사항 표시
                nowNum++;
                mainActivity.applyBackupProgress(dialog, 100 * (nowNum/totalNum));
                //Log.d("progress", "total : " + totalNum + " , now : " + nowNum);
            }

            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 사용자가 도중에 백업과정을 취소하지 않았을 경우
        if(ZIPFLAG == 1){
            mainActivity.startBackUpFileSendMail(fileName);
            ZIPFLAG = 0;
        }
        // 진행바 종료
        dialog.dismiss();
    }

    public void unzip(String _zipFile, String _targetLocation, ProgressDialog dialog, MainActivity mainActivity) {
        AESService aesService = new AESService();
        //create target location folder if not exist
        dirChecker(_targetLocation);
        // zip 시작 플래그
        ZIPFLAG = 1;
        try {
            FileInputStream fin = new FileInputStream(_zipFile);
            FileInputStream countFin = new FileInputStream(_zipFile);
            ZipInputStream zin = new ZipInputStream(fin);
            ZipInputStream countZin = new ZipInputStream(countFin);
            ZipEntry ze = null;

            // 진행사항 표시하기
            int totalNum = 0;
            while((ze = countZin.getNextEntry()) != null) totalNum++;
            int nowNum = 1;
            //Log.d("fileName", "totalNum : " + fin.available());

            while ((ze = zin.getNextEntry()) != null) {
                //Log.d("fileName", "ze : YES!!");
                //create dir if required while unzipping
                if (ze.isDirectory()) {
                    dirChecker(ze.getName());
                } else {
                    FileOutputStream fout = new FileOutputStream(_targetLocation + ze.getName());
                    for (int c = zin.read(); c != -1; c = zin.read()) {
                        fout.write(c);
                    }

                    zin.closeEntry();
                    fout.close();
                    // 복호화작업
                    aesService.decFile(mainActivity, ze.getName());

                    // 사용자가 도중에 취소할 경우
                    if(ZIPFLAG == 0){
                        break;
                    }
                    // 진행사항 표시
                    nowNum++;
                    mainActivity.applyBackupProgress(dialog, 100 * (nowNum/totalNum));
                }
            }
            zin.close();
        } catch (Exception e) {
            System.out.println(e);
        }

        // 사용자가 도중에 백업과정을 취소하지 않았을 경우
        if(ZIPFLAG == 1){
            ZIPFLAG = 0;
        }

        // 진행바 종료
        dialog.dismiss();
    }



    /**
     * 폴더 압축
     * @param inputFolderPath : 압축할 폴더 경로
     * @param outZipPath : 저장될 경로의 파일 이름
     */

    public void zipFolder(String inputFolderPath, String outZipPath) throws Exception {
        // 압축 대상(sourcePath)이 디렉토리나 파일이 아니면 리턴한다.
        File sourceFile = new File(inputFolderPath);
        if (!sourceFile.isFile() && !sourceFile.isDirectory()) {
            throw new Exception("압축 대상의 파일을 찾을 수가 없습니다.");
        }


        try (FileOutputStream fos = new FileOutputStream(outZipPath); BufferedOutputStream bos = new BufferedOutputStream(fos); ZipOutputStream zos = new ZipOutputStream(bos)) {
            // FileOutputStream
            // BufferedStream
            // ZipOutputStream
            zos.setLevel(COMPRESSION_LEVEL); // 압축 레벨 - 최대 압축률은 9, 디폴트 8

            zipEntry(sourceFile, inputFolderPath, zos); // Zip 파일 생성
            zos.finish(); // ZipOutputStream finish
        }

    }
    public void zipEntry(File sourceFile, String sourcePath, ZipOutputStream zos) throws Exception {
        // sourceFile 이 디렉토리인 경우 하위 파일 리스트 가져와 재귀호출
        if (sourceFile.isDirectory()) {
            if (sourceFile.getName().equalsIgnoreCase(".metadata")) { // .metadata 디렉토리 return
                return;
            }
            File[] fileArray = sourceFile.listFiles(); // sourceFile 의 하위 파일 리스트
            for (int i = 0; i < Objects.requireNonNull(fileArray).length; i++) {
                zipEntry(fileArray[i], sourcePath, zos); // 재귀 호출
            }
        } else { // sourcehFile 이 디렉토리가 아닌 경우
            BufferedInputStream bis = null;

            try {
                String sFilePath = sourceFile.getPath();
                //Log.i("aa", sFilePath);
                //String zipEntryName = sFilePath.substring(sourcePath.length() + 1, sFilePath.length());
                StringTokenizer tok = new StringTokenizer(sFilePath,"/");

                int tok_len = tok.countTokens();
                String zipEntryName=tok.toString();
                while(tok_len != 0){
                    tok_len--;
                    zipEntryName = tok.nextToken();
                }
                bis = new BufferedInputStream(new FileInputStream(sourceFile));

                ZipEntry zentry = new ZipEntry(zipEntryName);
                zentry.setTime(sourceFile.lastModified());
                zos.putNextEntry(zentry);

                byte[] buffer = new byte[BUFFER_SIZE];
                int cnt = 0;

                while ((cnt = bis.read(buffer, 0, BUFFER_SIZE)) != -1) {
                    zos.write(buffer, 0, cnt);
                }
                zos.closeEntry();
            } finally {
                if (bis != null) {
                    bis.close();
                }
            }
        }
    }




    private void dirChecker(String dir) {
        File f = new File(dir);
        if (!f.isDirectory()) {
            f.mkdirs();
        }
    }

    void stopProgress(){
        ZIPFLAG = 0;
    }
}