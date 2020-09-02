# PJ Notification

**PJ Notification** is open-source Android application, which pushes alert about course notifications for [YSCEC](https://yscec.yonsei.ac.kr/index.php).

Please note that **PJ Notification** is **unofficial** that is not granted anything from university.

## Important Note

From now, I will rarely update the repository because I think it is mature enough to use.

## Contributing on PJ Notification

Although there were much **help** from my friends and many **reviews** that motivated me, **PJ Notification** is developed only by myself for about a year, and I developed voluntarily without any financial support. However, I will really appreciate if you contribute **PJ Notification** by **any means**.

This includes
* **Icon Image**
* **Bug** Report
* **Merge Request** for New Features


Please contact via **email** ```steinsapk@gmail.com``` if **you have any questions**.

**Again, I will really appreciate any of them.**

## From Motivation to Creation

### Motivation

For many years, there was no official Android application for [YSCEC](https://yscec.yonsei.ac.kr/index.php) which can push alert for course notifications.
For me, it was really inconvenient to everytime login and check whether there is a new course notification for every courses.

Also, I sometimes encountered the situation that everybody knows about the notice except for me because I did not check the notice frequently.
So, I always wanted to make a easier way to check the course notifications.
I thought Android application was the best option because it can push alert so that I do not have to login and check notifications manually.

However, I had only a little knowledge about the Android so I had no way to realize my thoughts.

### Creation

In 1st semester, 2019, I applied for a leave of absence from university. While I was talking with one of my friend, I accidentally mentioned about the Android application for [YSCEC](https://yscec.yonsei.ac.kr/index.php). And he said

**"Why not?"**

Even though I did not have enough knowledge about that Android at that time, I was motivated and just started. While there were some obstacles especially concerning automatic login, I finally created push alert Android application for [YSCEC](https://yscec.yonsei.ac.kr/index.php) and published it in 27th, May, 2019.



## Google Play Store

You can check the **PJ Notification** [here](https://play.google.com/store/apps/details?id=org.steinsapk.pjnotification).



## Future Work

Further work would be

1. Supporting other university that does not have official notice system.
2. Supporting multi-language(English, Japanese, Chinese).
3. Optimize crawling routine
4. Remove HtmlUnit library
5. Modify UI
6. Receive new YSCEC message
7. Other new features such as crawling notice for department site.



## Note

README.md is not fully documented yet.
I will update it gradually.



## PJ Notification Pros

Here are some points why you should use **PJ Notification**.

### **1. PJ Notification is safe.**

There is no server that mediates between the client and [YSCEC](https://yscec.yonsei.ac.kr/index.php).
Thus, all of the work is done by the client and **no private information is passed to the developer's private server**.
This is really important because YSCEC ID and password are not only used for logging in to [YSCEC](https://yscec.yonsei.ac.kr/index.php) website, but also used for logging in to YONSEI portal and all the other things that are related to YONSEI university.

By acquiring the YSCEC ID and password, one can access all of the personal private data and here are the lists.

### From [Portal](https://portal.yonsei.ac.kr/main/index.jsp)
* Student Name
* 13 digits Social Security Number
* Birth Date
* Mobile Phone Number
* E-mail Address
* Address
* Year of University
* Major
* Date of Admission
* Admissions Track
* Bank Account Number
* Transcripts
* Scholarship Application Result

### From [YSCEC](https://yscec.yonsei.ac.kr/index.php)
* All Courses that have been Registered
* Course Materials
* Assignment Submissions
* [YSCEC](https://yscec.yonsei.ac.kr/index.php) Messages

In the worst case, if malicious person who has one's [YSCEC](https://yscec.yonsei.ac.kr/index.php) ID and password can cancel the course registration during registration period, or register a leave of absence from school.
Although it is just an assumption, as long as the server manages users' ID and password, there always will be risk for hacking, misuse by the server maintainer.

On the other hand, **PJ Notification has nothing to do with this security problem.**

Please read **"Why to This Design"** Part if you are interested in how I came up with this client-based Application.


### **2. PJ Notification is versatile.**

At first, it only supported notice board push alert which almost every course has and use widely.
However, I found out that all the new materials should be notified unless the user does not want to.
In fact, I felt inconvenient not getting push alert for every new materials.

Thus, I had fixed and updated the Application to push alert every materials, and now, the PJ Notification push alert for all of the materials such as files, assignment page, and other boards(QnA boards, Quiz boards, etc.).

Also, in the recent version, there was a new feature that users can chooose not to get push alert for some courses or boards. User can get push alert only what they want by disabling some of the courses and boards.
In the past, I just erased the notification when it seems useless such as some question notifications, but I found out that someone in the [Everytime](https://everytime.kr) said it is unconvenient to be notified for every single new writings in boards and materials.

**PJ Notification** will update more features, so it will be more versatile in the future.

### **3. PJ Notification is open-source.**

PJ Notification uses HtmlUnit library to login which is GPL licensed.
Thus, PJ Notification is also GPL licensed.
Although I am planning replace HtmlUnit with Jsoup completely, I will stick to the GPL License.

While it seems there is no benefit for users, again, this ensures that the **PJ Notification** is trustworthy.



## PJ Notification Cons

Pros always comes with Cons.

### 1. PJ Notification Consumes Battery.

While PJ Notification is safe because there is no server to store your personal data, it periodically runs on background to crawl new data.
Thus, this consumes battery and resources proportional to the periodic cycle to crawl.
This is inherent problem of **PJ Notification** and **should be minimized by optimizing the crawling operation.**

### 2. PJ Notification Is Not UI-Friendly.

Still, I do have have profound knowledge about Android and how to make clean UI.
Thus, I tried to mimic other websites and apps that seems simple but good-looking.

For example, the login page of **PJ Notification** was created by mimicing the Y-attend Android application.
At first I spent pretty much effort to the UI before releasing the app, and one of the result was the login page.

However, I started to concentrate on funtionality rather than UI because it took much time to make clean UI even for a single page.
I am thinking about applying new UI to **PJ Notification**.

### 3. PJ Notification Has Some Errors.

From the first release, there was a review that the application is crashing.
I did not know the reason because the application worked well for my smartphone.
At the end, it turns out that some of the Samsung smartphones crashes and I had a hard time finding it out and testing it because I did not have Samsung smartphone which crashes with **PJ Notification**.

There were many errors due to the HtmlUnit, and still, there are. I still do not know why it has an error because it ran without any errors on desktop and foreground task, but not for the background task.
Since I made a shift from HtmlUnit to Jsoup, errors has been decreased.

Still, there will be some errors that I have not found yet.
I also sometimes encounter some errors because of the network but I have not handled it yet.

I will try to fix errors and bugs as much as possible, but there always will be some cases that I cannot find and handle.

## Why to This Design?

### Crawling [YSCEC](https://yscec.yonsei.ac.kr/index.php) Design Pros
At first, I thought running a server for crawling is a good way to implement **PJ Notification**.

This is because
#### 1. I did not know about Android deeply.
Making a program running on server will reduce the work on the application and make the application simple.
#### 2. Running on a background would be limited by the Android system.

However, **this design was abandoned** because one of my friends who helped me a lot for developing this application said

### Crawling [YSCEC](https://yscec.yonsei.ac.kr/index.php) Design Cons

#### 1. Running a server to crawl would need a security and encryption.
I had little knowledge about the security and encryption. Although I can learn and find some good stuffs on the Internet, I cannot guarantee to provide a completely safe security.

#### 2. Although most of the websites only store the hash value for the password, the server to crawl needs a full password.
This is because while a normal website is checking whether the user is a valid user for its website, crawling server is accessing another web server to crawl. 

#### 3. The YSCEC server might block the crawling server.
To support many users, the crawling server should login with many different IDs. However, there is alwayas a risk that the maintainer of the [YSCEC](https://yscec.yonsei.ac.kr/index.php) server might find out and block.<br>
Although there are many shared computers on campus that students login with their own IDs and print the course materials, the crawling server would login much more frequently and continuously to support many users. Also, while the shared computers on campus have IP within Yonsei University, the crawling server would probably not have one.<br>
For me, I suspected it would rarely happen because I was doubting whether there was a maintainer of the server who cares about the access records. If the university paid much attention to [YSCEC](https://yscec.yonsei.ac.kr/index.php) server, why would they do not update the outdated webpage and develop an application that pushes alert?


### Directly Accessing to the [YSCEC](https://yscec.yonsei.ac.kr/index.php) DB Design
Instead of crawling, directly accessing the DB server would be much better way because crawling takes more time.
However, I did not try this approach because I thought it was not feasible.

Although there is an e-mail address on the login page of [YSCEC](https://yscec.yonsei.ac.kr/index.php), I thought they would not give me a permission to access because I was just a student without any community and there was no results at that time to support my request.

For these reasons that I just mentioned above, I developed **PJ Notification** without crawling server.

Although it is inconvenient to debug, because I do not have any accesses to the user's personal IDs, I think this design choice is good enough even though there is some drawbacks mentioned in the **PJ Notification** Cons part.