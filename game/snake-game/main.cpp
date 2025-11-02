#include <GL/glut.h>
#include <stdio.h>
#include <stdlib.h>
#include <time.h>
#include <vector>
#include <array>

// Initialize global variables
float x = 0.0f;     // Current X position of the snake head
float y = 0.0f;     // Current Y position of the snake head
float foodX = 0.5;  // X position of the food
float foodY = 0.5;  // Y position of the food
float incX = 0.01f; // Increment for X direction movement
float incY = 0.0f;  // Increment for Y direction movement

// Define the snake body using a vector of arrays, where each array stores the position of a segment
std::vector<std::array<float, 2>> points = {{-0.06, 0.0}, {-0.05, 0.0}, {-0.04, 0.0}, {-0.03, 0.0}, {-0.02, 0.0}, {-0.01, 0.0}}; // tail => head

// Function to make the snake go left
void goLeft()
{
    incX = -0.01f;
    incY = 0.0f;
}

// Function to make the snake go right
void goRight()
{
    incX = 0.01f;
    incY = 0.0f;
}

// Function to make the snake go up
void goUp()
{
    incX = 0.0f;
    incY = 0.01f;
}

// Function to make the snake go down
void goDown()
{
    incX = 0.0f;
    incY = -0.01f;
}

// Function to handle special keyboard keys (arrow keys)
void specialKey(int key, int x, int y)
{
    switch (key)
    {
    case GLUT_KEY_UP:
        goUp();
        break;
    case GLUT_KEY_DOWN:
        goDown();
        break;
    case GLUT_KEY_LEFT:
        goLeft();
        break;
    case GLUT_KEY_RIGHT:
        goRight();
        break;
    }
}

// Function to display the graphics
void display()
{
    glClear(GL_COLOR_BUFFER_BIT);
    glColor3f(1.0f, 1.0f, 1.0f);
    glPointSize(5.0f);

    glBegin(GL_POINTS);

    // Draw the snake head
    glVertex2f(x, y);

    // Draw each segment of the snake body
    for (int i = 0; i < points.size(); i++)
    {
        float temp[2] = {points[i][0], points[i][1]};
        glVertex2fv(temp);
    }

    // Draw the food
    glVertex2f(foodX, foodY);

    glEnd();
    glFlush();
}

// Function to update the game state and schedule the next update
void timer(int value)
{
    x += incX;
    y += incY;

    // Check if the snake has eaten the food
    if (((foodX - 0.02 <= x) && (x <= foodX + 0.02)) &&
        ((foodY - 0.02 <= y) && (y <= foodY + 0.02)))
    {
        points.push_back({x, y});
        foodX = (float)rand() / RAND_MAX;
        foodY = (float)rand() / RAND_MAX;
    }
    else
    {
        // Update the positions of the snake body segments
        for (int i = 0; i + 1 < points.size(); i++)
        {
            points[i][0] = points[i + 1][0];
            points[i][1] = points[i + 1][1];
        }
        points[points.size() - 1][0] = x;
        points[points.size() - 1][1] = y;
    }

    glutPostRedisplay();         // Schedule a window update
    glutTimerFunc(50, timer, 0); // Schedule the next timer event
}

int main(int argc, char **argv)
{
    srand(time(0)); // Seed the random number generator
    glutInit(&argc, argv);
    glutInitDisplayMode(GLUT_SINGLE | GLUT_RGB);
    glutInitWindowSize(500, 500);
    glutInitWindowPosition(100, 100);
    glutCreateWindow("Snake Game");
    glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
    glMatrixMode(GL_PROJECTION);
    glLoadIdentity();
    glutDisplayFunc(display);
    glutSpecialFunc(specialKey);
    glutTimerFunc(50, timer, 0); // Schedule the first timer event
    glutMainLoop();
    return 0;
}
