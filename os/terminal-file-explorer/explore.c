#include "headers.h"


typedef struct count {
    unsigned dir;
    unsigned reg_file;
} count;


count number_of_files(const char *path) {
    struct dirent *pDirent;
    DIR *pDir;

    pDir = opendir(path);
    if (pDir == NULL)
        fprintf(stderr, "Opening directory 1 '%s': %s\n", path, strerror(errno));

    count cnt = {0, 0};
    while ((pDirent = readdir(pDir)) != NULL) {
        if (strcmp(pDirent->d_name, ".") == 0  ||
            strcmp(pDirent->d_name, "..") == 0)
            continue;

        if (pDirent->d_type == DT_DIR)
            cnt.dir++;
        else if (pDirent->d_type == DT_REG)
            cnt.reg_file++;
    }

    if (errno != 0)
        fprintf(stderr, "%s\n", strerror(errno));

    closedir(pDir);
    return cnt;
}


void print_tree(char *path, count file_number, unsigned lvl) {
    if (lvl == 1)
        printf(".\n");

    struct dirent *pDirent;
    DIR *pDir;

    pDir = opendir(path);

    if (pDir == NULL)
        fprintf(stderr, "Opening directory 2 '%s': %s\n", path, strerror(errno));

    while ((pDirent = readdir(pDir)) != NULL) {
        if (strcmp(pDirent->d_name, ".") == 0  || strcmp(pDirent->d_name, "..") == 0) {
            file_number.dir--;
            continue;
        }

        if (pDirent->d_type == DT_DIR) {
            if ((pDirent->d_name)[0] == '.')
                continue;

            char tem_path[300];
            strcpy(tem_path, path);
            strcat(tem_path, "/");
            strcat(tem_path, pDirent->d_name);

            for (int i = 1; i <= lvl; i++) {
                if (i == lvl) {
                    printf(file_number.dir == 0 ? "└──" : "├──");
                    printf(" %s/\n", pDirent->d_name);
                } else {
                    printf("│   ");
                }
            }

            print_tree(tem_path, number_of_files(tem_path), lvl + 1);

        } else if (pDirent->d_type == DT_REG) {
            if ((pDirent->d_name)[0] == '.') {
                file_number.reg_file--;
                continue;
            }

            file_number.reg_file--;

            for (int i = 1; i <= lvl; i++) {
                if (i == lvl) {
                    printf(file_number.reg_file == 0 ? "└──" : "├──");
                    printf(" %s\n", pDirent->d_name);
                } else {
                    printf("│   ");
                }
            }
        }
    }

    if (errno != 0)
        fprintf(stderr, "%s\n", strerror(errno));

    closedir(pDir);
}


void print(char *path) {
    struct dirent *pDirent;
    DIR *pDir;

    count file_number = number_of_files(path);
    unsigned total_file = file_number.dir + file_number.reg_file;

    pDir = opendir(path);

    if (pDir == NULL)
        fprintf(stderr, "Opening directory '%s': %s\n", path, strerror(errno));

    for (int i = 0; (pDirent = readdir(pDir)) != NULL; i++) {

        if (strcmp((pDirent->d_name), ".") == 0  || strcmp((pDirent->d_name), "..") == 0)
            continue;

        if (pDirent->d_type == DT_DIR)
            printf("%s/\n", pDirent->d_name);
        else if (pDirent->d_type == DT_REG)
            printf("%s\n", pDirent->d_name);
    }

    if (errno != 0)
        fprintf(stderr, "%s\n", strerror(errno));

    closedir(pDir);
    free(pDirent);
}


int explore(char **words, const int word_cnt) {
    char *path = (word_cnt == 1 ? "." : words[1]);

    if (word_cnt == 3 && strcmp(words[2], "-t") == 0)
        print_tree(path, number_of_files(path), 1);
    else
        print(path);

    return 0;
}